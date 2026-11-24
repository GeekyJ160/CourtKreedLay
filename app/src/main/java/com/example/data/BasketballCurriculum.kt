package com.example.data

enum class DrillAnimationType {
    POUND_DRIBBLE,
    CROSSOVER_WEAVE,
    FORM_SHOOT,
    LAYUP_DRIVE,
    DEFENSIVE_SLIDE,
    FLOOR_VISION_PASS,
}

enum class LabCategory(val title: String, val subtitle: String) {
    HANDLES("Ball Handling Lab", "Crossovers, hesitations, weak-hand control, retreat dribbles"),
    SHOOTING("Shooting Lab", "Form, balance, release, footwork and off-ball accuracy"),
    FINISHING("Finishing Lab", "Both-hand layups, floaters and controlled contact finishes"),
    DEFENSE("Defensive Lab", "Lateral movement, closeouts, positioning and anticipation"),
    ATHLETIC("Athletic Development", "Balance, coordination, acceleration and first-step agility"),
    IQ("Basketball IQ", "Interactive plays, spacing, passing reads and decision-making"),
}

data class EvolutionLevel(
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val skillsSummary: String,
    val unlockCriteria: String,
    val minXp: Int,
    val minWorkouts: Int,
)

data class IqReadScenario(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

data class PlayerDnaModel(
    val id: String,
    val name: String,
    val archetype: String,
    val positionTag: String,
    val traits: List<String>,
    val mission: String,
    val filmStudyKeyPoints: List<String>,
    val signatureDrills: List<String>,
    val iqScenario: IqReadScenario,
    val usesPlaymakerArt: Boolean,
)

data class DrillItem(
    val id: String,
    val name: String,
    val durationSeconds: Int,
    val restSeconds: Int,
    val repsTarget: String,
    val coachingCue: String,
    val voiceCue: String,
    val labCategory: LabCategory,
    val animationType: DrillAnimationType,
    val primaryAttribute: String,
)

data class WorkoutProgram(
    val id: String,
    val dayKey: String,
    val title: String,
    val levelTag: String,
    val focusSummary: String,
    val durationMinutes: Int,
    val xpReward: Int,
    val drills: List<DrillItem>,
)

data class CourtZoneDef(
    val id: String,
    val name: String,
    val shortLabel: String,
    val normX: Float, // 0f..1f across court width
    val normY: Float, // 0f..1f from baseline (0f) to half-court (1f)
    val isCloseRange: Boolean,
    val recommendedDrill: String,
)

data class RoadmapPhase(
    val window: String,
    val title: String,
    val focus: String,
    val levelRange: IntRange,
)

object BasketballCurriculum {

    val affirmations = listOf(
        "I don't need permission to be great. I trust my preparation.",
        "Next play mentality — every miss is data, never doubt.",
        "I am the loudest voice on defense and the calmest guard on offense.",
        "Attack the open lane first. Make the defense react to me.",
        "My left hand is a weapon I sharpen every single day.",
        "I don't wait to be asked to lead — I set the tempo from the warmup."
    )

    val evolutionLevels = listOf(
        EvolutionLevel(
            levelNumber = 1,
            title = "Foundation",
            subtitle = "Starting Level",
            skillsSummary = "Stationary handles, weak hand, form shooting close in, layup footwork both hands, defensive stance.",
            unlockCriteria = "Starting level — complete baseline assessment & Day 01 Own the Ball.",
            minXp = 0,
            minWorkouts = 0
        ),
        EvolutionLevel(
            levelNumber = 2,
            title = "Handle Specialist",
            subtitle = "Progression Unlock",
            skillsSummary = "Crossovers, between-the-legs, behind-the-back, hesitation, retreat dribbles, two-ball drills.",
            unlockCriteria = "400+ XP and 3 completed workouts (or Parent Mastery Sign-Off).",
            minXp = 400,
            minWorkouts = 3
        ),
        EvolutionLevel(
            levelNumber = 3,
            title = "Shot Creator",
            subtitle = "Progression Unlock",
            skillsSummary = "Dribble pull-ups, catch-and-shoot, shooting off screens, floaters, jab steps & off-ball movement.",
            unlockCriteria = "900+ XP and 6 completed workouts (or Parent Mastery Sign-Off).",
            minXp = 900,
            minWorkouts = 6
        ),
        EvolutionLevel(
            levelNumber = 4,
            title = "The Attacker",
            subtitle = "Progression Unlock",
            skillsSummary = "1-on-1 moves, explosive driving, finishing through contact, drive-and-kick reads.",
            unlockCriteria = "1,500+ XP and 10 completed workouts (or Parent Mastery Sign-Off).",
            minXp = 1500,
            minWorkouts = 10
        ),
        EvolutionLevel(
            levelNumber = 5,
            title = "Floor General",
            subtitle = "Progression Unlock",
            skillsSummary = "Calling plays, transition decisions, leading a break, communication & taking the big shot.",
            unlockCriteria = "2,200+ XP and 14 completed workouts (or Parent Mastery Sign-Off).",
            minXp = 2200,
            minWorkouts = 14
        ),
        EvolutionLevel(
            levelNumber = 6,
            title = "Game Ready",
            subtitle = "Tryout Peak",
            skillsSummary = "Middle school tryout simulations, defensive anticipation, and full game-speed execution.",
            unlockCriteria = "3,000+ XP and 18 completed workouts (or Parent Mastery Sign-Off).",
            minXp = 3000,
            minWorkouts = 18
        )
    )

    val playerDnaModels = listOf(
        PlayerDnaModel(
            id = "clark",
            name = "Caitlin Clark",
            archetype = "The Playmaker",
            positionTag = "PG · Vision & Range",
            traits = listOf("Deep shooting confidence", "Hit-ahead transition vision", "Tempo control", "Fearless pull-up"),
            mission = "See the open teammate before the defense reacts.",
            filmStudyKeyPoints = listOf(
                "Eyes up the floor immediately on every defensive rebound — look for the outlet sprint lane first.",
                "Shoot the open look without double-clutching; your range stretches defenders and opens passing windows.",
                "Use pace changes off screens to force the help defender to commit before delivering the pocket pass."
            ),
            signatureDrills = listOf("Eyes-Up Outlet & Hit-Ahead", "Step-Into-Range Catch & Shoot", "Pick-and-Roll Pocket Pass"),
            iqScenario = IqReadScenario(
                question = "You push the ball in transition. Two defenders sprint back to stop the ball near the free-throw line while your wing sprints to the corner. What's the read?",
                options = listOf(
                    "Stop dribbling at half-court and wait for everyone",
                    "Dribble directly into the two defenders with your head down",
                    "Keep eyes on the rim to freeze them, then snap an early hit-ahead pass to the corner"
                ),
                correctIndex = 2,
                explanation = "Threatening the paint forces both defenders to collapse, creating a clean catch-and-shoot window before the defense rotates."
            ),
            usesPlaymakerArt = true
        ),
        PlayerDnaModel(
            id = "bueckers",
            name = "Paige Bueckers",
            archetype = "The Technician",
            positionTag = "PG / SG · Midrange & Control",
            traits = listOf("Midrange pull-up mastery", "Composure under pressure", "Zero wasted dribbles", "Vocal leadership"),
            mission = "Play at your own pace without letting defenders rush you.",
            filmStudyKeyPoints = listOf(
                "Never let an aggressive on-ball defender speed you up — use a calm hesitation dribble and low shoulder seal.",
                "Master the 12-to-15 foot pull-up off one or two hard dribbles when the paint is crowded.",
                "Stay balanced on every stop (jump stop or 1-2 footwork) so you always have pass, pivot, or shot."
            ),
            signatureDrills = listOf("1-Dribble Midrange Pull-Up", "Hesitation-to-Floater", "Calm Press-Break Escape"),
            iqScenario = IqReadScenario(
                question = "The defender cuts off your drive at the elbow and the shot-blocker steps up in the paint. What is the Technician move?",
                options = listOf(
                    "Force a wild layup into two defenders",
                    "Jump stop on two feet, rise smoothly for a balanced 13-foot pull-up",
                    "Pick up your dribble and panic-pass backward"
                ),
                correctIndex = 1,
                explanation = "Paige thrives in that midrange pocket between the perimeter defender and the rim protector — controlled, balanced, and unhurried."
            ),
            usesPlaymakerArt = true
        ),
        PlayerDnaModel(
            id = "watkins",
            name = "JuJu Watkins",
            archetype = "The Attacker",
            positionTag = "SG · Rim Pressure & Assertiveness",
            traits = listOf("Explosive first step", "Attacking open gaps", "Strong contact finishes", "Zero hesitation"),
            mission = "Attack an open lane instead of automatically passing.",
            filmStudyKeyPoints = listOf(
                "Rip the ball aggressively below your knees or above your head — protect the rock and win the first step.",
                "Read the defender's top foot: if she gives you a gap, attack the hip immediately.",
                "Finish through bumps by absorbing contact with your inside shoulder and keeping eyes locked on the backboard square."
            ),
            signatureDrills = listOf("Rip-Through Attack Drive", "Contact Pad Finish (Both Hands)", "Euro-Step & Pro-Hop"),
            iqScenario = IqReadScenario(
                question = "You catch the ball on the wing and your defender closes out wildly off-balance with her feet narrow. What should you do?",
                options = listOf(
                    "Immediately pass it back to the top of the key",
                    "Jab-and-rip straight past her hip to attack the rim",
                    "Hold the ball for 4 seconds to let the defense reset"
                ),
                correctIndex = 1,
                explanation = "Great guards punish sloppy closeouts right away. Attacking the open lane creates either a layup, free throws, or a wide-open kickout."
            ),
            usesPlaymakerArt = false
        ),
        PlayerDnaModel(
            id = "curry",
            name = "Stephen Curry",
            archetype = "The Sharpshooter",
            positionTag = "SG / PG · Form & Off-Ball Motion",
            traits = listOf("Compact one-motion shot", "Constant off-ball relocation", "Quick footwork prep", "Conditioned handles"),
            mission = "Move into scoring position before receiving the ball.",
            filmStudyKeyPoints = listOf(
                "Shoot prep starts BEFORE the catch: feet loaded, hips slightly dropped, hands showing a target.",
                "Never stand and watch after you pass — cut, screen away, or relocate to an open shooting window.",
                "Keep your guide hand quiet and follow through high until the ball hits the rim."
            ),
            signatureDrills = listOf("Close-Range Form Swishes", "Pass-Relocate Catch & Shoot", "Pin-Down Screen Footwork"),
            iqScenario = IqReadScenario(
                question = "You pass the ball to the point guard on the wing, and your defender turns her head to watch the ball. What's your next move?",
                options = listOf(
                    "Stand in place and rest on the weak side",
                    "Backdoor cut or relocate into her blind spot for an open shot",
                    "Call for a timeout"
                ),
                correctIndex = 1,
                explanation = "When a defender watches the ball instead of you, relocating or cutting into open space creates effortless scoring chances."
            ),
            usesPlaymakerArt = false
        ),
        PlayerDnaModel(
            id = "gray",
            name = "Chelsea Gray",
            archetype = "The Floor General",
            positionTag = "PG · Passing Creativity & Leadership",
            traits = listOf("Patience in the paint", "Wrap-around & bounce passes", "Reading help defense", "Vocal team commander"),
            mission = "Read the help defender's chest before making your pass.",
            filmStudyKeyPoints = listOf(
                "Probe into the paint under control — stay on two feet (stride stop) so you never trap yourself.",
                "Use eye fakes to move the weak-side defender before snapping a crisp chest or bounce pass.",
                "Organize teammates early: call out sets, point to cutters, and celebrate every hustle play."
            ),
            signatureDrills = listOf("Paint Stride-Stop & Kick", "Left/Right One-Hand Target Passes", "2-Minute Huddle Command"),
            iqScenario = IqReadScenario(
                question = "You drive into the lane and the weak-side post defender steps over to block your layup, leaving your post teammate open near the rim. What do you read?",
                options = listOf(
                    "Keep your eyes down and jump anyway",
                    "See the help defender's chest commit to you, jump-stop, and deliver a pocket bounce pass",
                    "Dribble all the way back out to half-court"
                ),
                correctIndex = 1,
                explanation = "Floor generals read the help defender early and turn double-teams into easy layups for teammates."
            ),
            usesPlaymakerArt = true
        )
    )

    val day01OwnTheBall = WorkoutProgram(
        id = "day_01_own_the_ball",
        dayKey = "DAY_01",
        title = "Day 01 — Own the Ball",
        levelTag = "Level 1 · Foundation",
        focusSummary = "Today's focus: ball control, close-range shooting mechanics, weak-hand layups & fearless confidence.",
        durationMinutes = 30,
        xpReward = 150,
        drills = listOf(
            DrillItem(
                id = "d1_warmup",
                name = "Dynamic Warmup & Stance",
                durationSeconds = 240,
                restSeconds = 20,
                repsTarget = "4 min · High-knee skips, defensive slides, ball wraps",
                coachingCue = "Stay low in an athletic stance. Chest up, eyes forward, active hands.",
                voiceCue = "Start your dynamic warmup. Stay low, wake up your feet, and wrap the ball smoothly around waist and ankles.",
                labCategory = LabCategory.ATHLETIC,
                animationType = DrillAnimationType.DEFENSIVE_SLIDE,
                primaryAttribute = "Defense"
            ),
            DrillItem(
                id = "d1_pounds",
                name = "Right & Left Hand Pound Dribbles",
                durationSeconds = 180,
                restSeconds = 20,
                repsTarget = "3 sets x 30 hard pounds each hand",
                coachingCue = "Pound the ball through the hardwood at waist height. Keep your chin up and look at the rim!",
                voiceCue = "Pound dribbles! Right hand first, then switch to left. Hard dribbles, chin up, never look at the ball.",
                labCategory = LabCategory.HANDLES,
                animationType = DrillAnimationType.POUND_DRIBBLE,
                primaryAttribute = "Weak Hand"
            ),
            DrillItem(
                id = "d1_cross",
                name = "Crossovers & Change of Pace",
                durationSeconds = 240,
                restSeconds = 25,
                repsTarget = "4 sets x 10 explosive crossovers",
                coachingCue = "Slow-to-fast! Sell the hesitation, then snap the ball wide and low across your shins.",
                voiceCue = "Crossovers and change of pace. Sell the slow hesitate, then explode low across your knees.",
                labCategory = LabCategory.HANDLES,
                animationType = DrillAnimationType.CROSSOVER_WEAVE,
                primaryAttribute = "Ball Handling"
            ),
            DrillItem(
                id = "d1_form",
                name = "Close-Range Form Shooting",
                durationSeconds = 300,
                restSeconds = 25,
                repsTarget = "30 swishes from 3–6 feet (1-hand + 2-hand)",
                coachingCue = "Elbow under the ball, soft wrist snap, hold your follow-through until the ball drops through the net.",
                voiceCue = "Close range form shooting. Focus on balance, elbow aligned, and hold your gooseneck follow-through.",
                labCategory = LabCategory.SHOOTING,
                animationType = DrillAnimationType.FORM_SHOOT,
                primaryAttribute = "Shooting"
            ),
            DrillItem(
                id = "d1_layups",
                name = "Strong & Weak-Hand Layups",
                durationSeconds = 300,
                restSeconds = 25,
                repsTarget = "12 makes right hand · 12 makes left hand",
                coachingCue = "Right-left-up on the right; left-right-up on the left. Aim softly for the top corner of the box.",
                voiceCue = "Both-hand layups! Drive with purpose, jump off the opposite foot, and kiss it off the top corner of the square.",
                labCategory = LabCategory.FINISHING,
                animationType = DrillAnimationType.LAYUP_DRIVE,
                primaryAttribute = "Finishing"
            ),
            DrillItem(
                id = "d1_catch_shoot",
                name = "Catch-and-Shoot Practice",
                durationSeconds = 300,
                restSeconds = 20,
                repsTarget = "25 game-speed shots from midrange spots",
                coachingCue = "Spin the ball out to yourself, step 1-2 into your shot, and shoot with zero hesitation.",
                voiceCue = "Catch and shoot! Spin the ball out, hop or step 1-2 with feet ready, and rise up confidently.",
                labCategory = LabCategory.SHOOTING,
                animationType = DrillAnimationType.FORM_SHOOT,
                primaryAttribute = "Shooting"
            ),
            DrillItem(
                id = "d1_mindset",
                name = "Confidence Challenge & Cooldown",
                durationSeconds = 240,
                restSeconds = 0,
                repsTarget = "5 assertive voice calls + 3 deep breaths",
                coachingCue = "Say your play calls out loud: 'Ball! Screen left! I got help!' Confidence starts with your voice.",
                voiceCue = "Confidence challenge and cooldown. Speak your court calls out loud with command, then breathe and visualize your next game.",
                labCategory = LabCategory.IQ,
                animationType = DrillAnimationType.FLOOR_VISION_PASS,
                primaryAttribute = "Confidence"
            )
        )
    )

    val allWorkouts: List<WorkoutProgram> = listOf(
        day01OwnTheBall,
        WorkoutProgram(
            id = "mon_handles_weak_hand",
            dayKey = "MON",
            title = "Monday — Handles & Weak Hand",
            levelTag = "Level 1–2 · Handle Focus",
            focusSummary = "Sharpen your off-hand dribble until both sides feel equally dangerous under pressure.",
            durationMinutes = 30,
            xpReward = 150,
            drills = listOf(
                day01OwnTheBall.drills[0],
                day01OwnTheBall.drills[1],
                DrillItem(
                    id = "mon_v_dribble",
                    name = "Left-Hand In-and-Out & Retreat",
                    durationSeconds = 300,
                    restSeconds = 20,
                    repsTarget = "4 sets x 45 sec weak-hand control",
                    coachingCue = "Sell the inside shoulder fake, then pull back smoothly into a protected retreat dribble.",
                    voiceCue = "Weak hand in-and-out into retreat dribble. Stay balanced and keep your right arm guarding the ball.",
                    labCategory = LabCategory.HANDLES,
                    animationType = DrillAnimationType.POUND_DRIBBLE,
                    primaryAttribute = "Weak Hand"
                ),
                day01OwnTheBall.drills[2],
                DrillItem(
                    id = "mon_left_finish",
                    name = "Left-Side Driving Layups & Floaters",
                    durationSeconds = 360,
                    restSeconds = 25,
                    repsTarget = "15 left-hand finishes off 1 dribble",
                    coachingCue = "Attack the left lane without switching back to your right hand. Trust your left!",
                    voiceCue = "Left-side finishes only! Attack the rim off one dribble and finish high with your left hand.",
                    labCategory = LabCategory.FINISHING,
                    animationType = DrillAnimationType.LAYUP_DRIVE,
                    primaryAttribute = "Weak Hand"
                ),
                day01OwnTheBall.drills[6]
            )
        ),
        WorkoutProgram(
            id = "tue_shooting_finishing",
            dayKey = "TUE",
            title = "Tuesday — Shooting & Finishing",
            levelTag = "Level 1–3 · Scorer Lab",
            focusSummary = "Build repeatable Steph Curry shooting form and JuJu Watkins rim finishes.",
            durationMinutes = 30,
            xpReward = 160,
            drills = listOf(
                day01OwnTheBall.drills[3],
                day01OwnTheBall.drills[5],
                DrillItem(
                    id = "tue_pullup",
                    name = "1-Dribble Midrange Pull-Up (Paige Pace)",
                    durationSeconds = 360,
                    restSeconds = 25,
                    repsTarget = "20 makes from elbows & wings",
                    coachingCue = "Hard attack dribble, stop on a dime, and elevate straight up — no drifting sideways.",
                    voiceCue = "One-dribble midrange pull-ups. Hard dribble, crisp stop, elevate straight up.",
                    labCategory = LabCategory.SHOOTING,
                    animationType = DrillAnimationType.FORM_SHOOT,
                    primaryAttribute = "Shooting"
                ),
                day01OwnTheBall.drills[4],
                DrillItem(
                    id = "tue_ft",
                    name = "Pressure Free Throws (Make 2 in a Row)",
                    durationSeconds = 240,
                    restSeconds = 0,
                    repsTarget = "5 rounds of 2-for-2 clutch free throws",
                    coachingCue = "Use the same routine every time. Deep breath, eyes on the back rim, soft release.",
                    voiceCue = "Pressure free throws. Slow down your breathing, trust your routine, and knock down two in a row.",
                    labCategory = LabCategory.SHOOTING,
                    animationType = DrillAnimationType.FORM_SHOOT,
                    primaryAttribute = "Confidence"
                )
            )
        ),
        WorkoutProgram(
            id = "wed_film_recovery",
            dayKey = "WED",
            title = "Wednesday — Recovery & Film Study",
            levelTag = "15 min · Mind & Body",
            focusSummary = "Active stretching, light fingertips ball control, and Player DNA film reads.",
            durationMinutes = 15,
            xpReward = 100,
            drills = listOf(
                DrillItem(
                    id = "wed_stretch",
                    name = "Mobility, Ankles & Hip Flow",
                    durationSeconds = 300,
                    restSeconds = 15,
                    repsTarget = "5 min gentle stretch & balance holds",
                    coachingCue = "Take care of knees, ankles, and hips after school practices.",
                    voiceCue = "Recovery mobility flow. Slow breathing and smooth stretches.",
                    labCategory = LabCategory.ATHLETIC,
                    animationType = DrillAnimationType.DEFENSIVE_SLIDE,
                    primaryAttribute = "Defense"
                ),
                DrillItem(
                    id = "wed_finger",
                    name = "Fingertip Ball Taps & Figure-8s",
                    durationSeconds = 300,
                    restSeconds = 15,
                    repsTarget = "5 min quiet touch drills",
                    coachingCue = "Keep the ball on your finger pads, not your palms.",
                    voiceCue = "Fingertip ball taps and figure-8 wraps. Smooth, effortless touch.",
                    labCategory = LabCategory.HANDLES,
                    animationType = DrillAnimationType.POUND_DRIBBLE,
                    primaryAttribute = "Ball Handling"
                ),
                DrillItem(
                    id = "wed_iq",
                    name = "Film Study Visualization (Clark & Gray)",
                    durationSeconds = 300,
                    restSeconds = 0,
                    repsTarget = "3 mental game reads + 1 Player DNA review",
                    coachingCue = "Visualize yourself calling the play, hitting the open wing, and celebrating with teammates.",
                    voiceCue = "Film study visualization. Picture yourself commanding the floor with calm confidence.",
                    labCategory = LabCategory.IQ,
                    animationType = DrillAnimationType.FLOOR_VISION_PASS,
                    primaryAttribute = "Court Vision"
                )
            )
        ),
        WorkoutProgram(
            id = "thu_shot_creation",
            dayKey = "THU",
            title = "Thursday — Shot Creation & Footwork",
            levelTag = "Level 3 · Create Your Shot",
            focusSummary = "Jab steps, off-ball cuts, floaters, and shooting off the dribble.",
            durationMinutes = 30,
            xpReward = 165,
            drills = listOf(
                day01OwnTheBall.drills[0],
                DrillItem(
                    id = "thu_jab",
                    name = "Triple-Threat Jab & Rip Series",
                    durationSeconds = 300,
                    restSeconds = 20,
                    repsTarget = "20 reps from both wings",
                    coachingCue = "Short, sharp jab toward the defender's front foot. Read her reaction: shoot if she drops, drive if she reaches.",
                    voiceCue = "Triple threat jab and rip! Read the defender's feet and attack decisively.",
                    labCategory = LabCategory.SHOOTING,
                    animationType = DrillAnimationType.CROSSOVER_WEAVE,
                    primaryAttribute = "Shooting"
                ),
                DrillItem(
                    id = "thu_floater",
                    name = "High-Arc Lane Floaters",
                    durationSeconds = 300,
                    restSeconds = 25,
                    repsTarget = "15 makes over imaginary tall defender",
                    coachingCue = "Soft touch high over the rim protector's reach. Finish balanced on two feet.",
                    voiceCue = "High-arc floaters in the lane. Soft release, high arc over the defense.",
                    labCategory = LabCategory.FINISHING,
                    animationType = DrillAnimationType.LAYUP_DRIVE,
                    primaryAttribute = "Finishing"
                ),
                day01OwnTheBall.drills[5],
                day01OwnTheBall.drills[6]
            )
        ),
        WorkoutProgram(
            id = "fri_defense_iq_leadership",
            dayKey = "FRI",
            title = "Friday — Defense, IQ & Leadership",
            levelTag = "Level 4–5 · Floor General",
            focusSummary = "Defensive closeouts, drive-and-kick reads, and leading the court with your voice.",
            durationMinutes = 30,
            xpReward = 175,
            drills = listOf(
                DrillItem(
                    id = "fri_closeout",
                    name = "Sprint Closeouts & Lateral Slides",
                    durationSeconds = 300,
                    restSeconds = 25,
                    repsTarget = "4 sets x 6 closeouts with loud 'BALL!' call",
                    coachingCue = "Sprint two-thirds, chop your feet low on the final third, high hand to contest without fouling.",
                    voiceCue = "Sprint closeouts! Chop your feet, call out BALL loudly, and slide laterally without crossing your feet.",
                    labCategory = LabCategory.DEFENSE,
                    animationType = DrillAnimationType.DEFENSIVE_SLIDE,
                    primaryAttribute = "Defense"
                ),
                DrillItem(
                    id = "fri_drive_kick",
                    name = "Drive-and-Kick Pocket Pass Drill",
                    durationSeconds = 360,
                    restSeconds = 25,
                    repsTarget = "20 crisp bounce/chest passes off dribble",
                    coachingCue = "Attack the paint with two feet, eyes up, snap a firm target pass with either hand.",
                    voiceCue = "Drive and kick! Attack the gap, read the help defender, and snap a crisp pass.",
                    labCategory = LabCategory.IQ,
                    animationType = DrillAnimationType.FLOOR_VISION_PASS,
                    primaryAttribute = "Court Vision"
                ),
                DrillItem(
                    id = "fri_command",
                    name = "2-Minute Floor General Command",
                    durationSeconds = 300,
                    restSeconds = 20,
                    repsTarget = "Call 5 plays & direct imaginary teammates",
                    coachingCue = "Point, clap, and speak clearly. Middle school coaches notice vocal guards immediately!",
                    voiceCue = "Floor general command. Call your offensive sets out loud and bring positive energy.",
                    labCategory = LabCategory.IQ,
                    animationType = DrillAnimationType.FLOOR_VISION_PASS,
                    primaryAttribute = "Leadership"
                ),
                day01OwnTheBall.drills[4]
            )
        )
    )

    val courtZones = listOf(
        CourtZoneDef("paint", "Paint / Rim", "PAINT", 0.50f, 0.20f, true, "Close-Range Form Shooting & Both-Hand Layups"),
        CourtZoneDef("left_short", "Left Short Corner", "L SHORT", 0.22f, 0.16f, true, "Bank-Shot Form & Baseline Floaters"),
        CourtZoneDef("right_short", "Right Short Corner", "R SHORT", 0.78f, 0.16f, true, "Bank-Shot Form & Baseline Floaters"),
        CourtZoneDef("left_elbow", "Left Elbow", "L ELBOW", 0.34f, 0.42f, true, "Paige 1-Dribble Midrange Pull-Up"),
        CourtZoneDef("right_elbow", "Right Elbow", "R ELBOW", 0.66f, 0.42f, true, "Paige 1-Dribble Midrange Pull-Up"),
        CourtZoneDef("ft_top", "Free Throw / Top Key", "FT LINE", 0.50f, 0.45f, true, "Pressure Free Throws & Jump-Stop Pull-Up"),
        CourtZoneDef("left_corner_3", "Left Corner 3", "L CRN 3", 0.09f, 0.14f, false, "Curry Corner Catch-and-Shoot Footwork"),
        CourtZoneDef("right_corner_3", "Right Corner 3", "R CRN 3", 0.91f, 0.14f, false, "Curry Corner Catch-and-Shoot Footwork"),
        CourtZoneDef("left_wing_3", "Left Wing 3", "L WING 3", 0.14f, 0.56f, false, "Relocation Wing Catch & Shoot"),
        CourtZoneDef("right_wing_3", "Right Wing 3", "R WING 3", 0.86f, 0.56f, false, "Relocation Wing Catch & Shoot"),
        CourtZoneDef("top_key_3", "Top of Key 3", "TOP 3", 0.50f, 0.72f, false, "Caitlin Step-Into-Range 3PT"),
    )

    val defaultMissions = listOf(
        Triple("Call for the ball five times during a game.", "LEADERSHIP", 5),
        Triple("Take an open shot without hesitating.", "CONFIDENCE", 3),
        Triple("Attack the basket when a driving lane opens.", "AGGRESSION", 3),
        Triple("Encourage a teammate after a mistake.", "LEADERSHIP", 3),
        Triple("Communicate loudly on defense.", "LEADERSHIP", 5),
        Triple("Lead the team warmup for two minutes.", "LEADERSHIP", 1),
        Triple("Recover confidently after missing a shot.", "CONFIDENCE", 3),
    )

    val defaultSchedule = listOf(
        Triple("Monday", "Handles and weak hand", 30),
        Triple("Tuesday", "Shooting mechanics and finishing", 30),
        Triple("Wednesday", "Recovery and film study", 15),
        Triple("Thursday", "Shot creation and footwork", 30),
        Triple("Friday", "Defense, IQ and leadership", 30),
        Triple("Saturday", "Pickup games or parent-led practice", 45),
        Triple("Sunday", "Rest and weekly reflection", 0),
    )

    val roadmapPhases = listOf(
        RoadmapPhase(
            window = "Nov – Dec 2026",
            title = "Phase 1 · Foundation & Mechanics",
            focus = "Baseline assessment, stationary handles, close-range form shooting, and confident daily habits.",
            levelRange = 1..1
        ),
        RoadmapPhase(
            window = "Jan – Feb 2027",
            title = "Phase 2 · Weak Hand & Reliability",
            focus = "Left-hand ball control, defensive footwork, and consistent midrange mechanics.",
            levelRange = 2..2
        ),
        RoadmapPhase(
            window = "Mar – Apr 2027",
            title = "Phase 3 · Shot Creation & Pace",
            focus = "Dribble pull-ups, change of speed, floaters, and assertive offensive reads.",
            levelRange = 3..3
        ),
        RoadmapPhase(
            window = "May – Jun 2027",
            title = "Phase 4 · Attack & Court Vision",
            focus = "Attacking open gaps, finishing through contact, and drive-and-kick passing windows.",
            levelRange = 4..4
        ),
        RoadmapPhase(
            window = "Jul – Aug 2027",
            title = "Phase 5 · Floor General & Tryout Sim",
            focus = "Game-speed execution, vocal leadership, calling plays, and tryout pressure simulations.",
            levelRange = 5..6
        ),
        RoadmapPhase(
            window = "Middle School Tryouts",
            title = "Game Ready · Own Your Court",
            focus = "Arrive prepared, conditioned, confident, and coachable — believing you belong.",
            levelRange = 6..6
        ),
    )
}
