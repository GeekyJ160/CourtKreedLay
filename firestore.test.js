const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_player_01";
const BOB_UID = "bob_player_02";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, now) {
  return {
    userId: uid,
    playerName: "Future Floor General",
    position: "PG / SG",
    gradYear: 2032,
    currentLevel: 1,
    xp: 250,
    streakDays: 3,
    dailyAffirmation: "I don't need permission to be great. I trust my preparation.",
    assignedFocus: "Left-hand control & confident catch-and-shoot",
    weeklyGoal: "Call for the ball 5 times every pickup game",
    fatigueLevel: "FRESH",
    tryoutReadiness: 48,
    shooting: 65,
    ballHandling: 40,
    finishing: 45,
    defense: 35,
    courtVision: 40,
    confidence: 35,
    leadership: 30,
    weakHand: 25,
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("workouts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("shotZones").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("mindsetMissions").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("observations").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("schedule").get());
});

test("Owner can create and partially update PlayerProfile; ghost fields & bad bounds fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const userDoc = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(userDoc.set(validProfilePayload(ALICE_UID, created)));
  await assertSucceeds(userDoc.get());

  // Partial update
  await assertSucceeds(
    userDoc.update({
      xp: 350,
      confidence: 42,
      fatigueLevel: "MODERATE",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject list on /users
  await assertFails(aliceDb.collection("users").get());

  // Reject immutable createdAt mutation
  await assertFails(
    userDoc.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject out-of-range rating > 99
  await assertFails(
    userDoc.update({
      shooting: 120,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field
  await assertFails(
    userDoc.update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user isolation blocks Bob from accessing Alice's profile and subcollections", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created));
    await db.collection("users").doc(ALICE_UID).collection("workouts").doc("w1").set({
      userId: ALICE_UID,
      dayKey: "MON",
      title: "Day 01 — Own the Ball",
      focusArea: "Handles & Weak Hand",
      durationMinutes: 30,
      drillsCompleted: 7,
      totalDrills: 7,
      xpEarned: 150,
      rpeFatigue: 5,
      createdAt: created,
      updatedAt: created,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ xp: 999, updatedAt: new Date(Date.now() - 1000) }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("workouts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w1").get());
});

test("Workouts, ShotZones, MindsetMissions, Observations, and Schedule CRUD & invariants", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const updated = new Date(Date.now() - 1000);
  const userRef = aliceDb.collection("users").doc(ALICE_UID);

  // 1. Workouts (without optional notes, and with optional notes)
  await assertSucceeds(
    userRef.collection("workouts").doc("w_1").set({
      userId: ALICE_UID,
      dayKey: "MON",
      title: "Day 01 — Own the Ball",
      focusArea: "Foundation",
      durationMinutes: 30,
      drillsCompleted: 7,
      totalDrills: 7,
      xpEarned: 150,
      rpeFatigue: 6,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(userRef.collection("workouts").get());
  // Reject @DocumentId "id" key
  await assertFails(
    userRef.collection("workouts").doc("w_bad").set({
      id: "w_bad",
      userId: ALICE_UID,
      dayKey: "TUE",
      title: "Bad Doc",
      focusArea: "Shooting",
      durationMinutes: 30,
      drillsCompleted: 5,
      totalDrills: 5,
      xpEarned: 100,
      rpeFatigue: 4,
      createdAt: created,
      updatedAt: created,
    })
  );

  // 2. ShotZones (valid vs makes > attempts)
  const zoneRef = userRef.collection("shotZones").doc("paint");
  await assertSucceeds(
    zoneRef.set({
      userId: ALICE_UID,
      zoneName: "Paint",
      makes: 8,
      attempts: 10,
      sessionMakes: 8,
      sessionAttempts: 10,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    zoneRef.update({
      makes: 9,
      attempts: 11,
      sessionMakes: 9,
      sessionAttempts: 11,
      updatedAt: updated,
    })
  );
  await assertFails(
    zoneRef.update({
      makes: 15,
      attempts: 11,
      updatedAt: updated,
    })
  );

  // 3. MindsetMissions (valid & reject future timestamp)
  const missionRef = userRef.collection("mindsetMissions").doc("m_1");
  await assertSucceeds(
    missionRef.set({
      userId: ALICE_UID,
      title: "Call for the ball five times during a game.",
      category: "LEADERSHIP",
      completed: false,
      targetCount: 5,
      currentCount: 2,
      xpReward: 75,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    missionRef.update({
      completed: true,
      currentCount: 5,
      updatedAt: updated,
    })
  );
  await assertFails(
    missionRef.update({
      completed: false,
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // 4. Observations
  await assertSucceeds(
    userRef.collection("observations").doc("obs_1").set({
      userId: ALICE_UID,
      authorRole: "PARENT",
      focusTag: "Weak Hand",
      note: "Attacked left twice in transition without hesitating.",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertFails(
    userRef.collection("observations").doc("obs_bad").set({
      userId: ALICE_UID,
      authorRole: "STRANGER",
      focusTag: "Weak Hand",
      note: "Invalid role",
      createdAt: created,
      updatedAt: created,
    })
  );

  // 5. Schedule
  await assertSucceeds(
    userRef.collection("schedule").doc("day_0").set({
      userId: ALICE_UID,
      dayIndex: 0,
      dayLabel: "Monday",
      focusTitle: "Handles and weak hand",
      durationMinutes: 30,
      schoolLoad: "PRACTICE",
      completedThisWeek: false,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    userRef.collection("schedule").doc("day_0").update({
      completedThisWeek: true,
      updatedAt: updated,
    })
  );
});
