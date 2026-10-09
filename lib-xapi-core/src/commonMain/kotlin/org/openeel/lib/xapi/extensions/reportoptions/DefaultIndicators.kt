package world.respect.lib.xapi.extensions.reportoptions

//TODO Need to change with string resource

object DefaultIndicators {
    val list = listOf(
        // Duration Metrics
        Indicator(
            name = "Total content usage duration",
            type = YAxisTypes.DURATION.name,
            description = "The cumulative amount of time spent by all users engaging with educational content, measured in hours and minutes"
        ),
        Indicator(
            name = "Average content usage duration per user",
            type = YAxisTypes.DURATION.name,
            description = "The mean time spent by individual users interacting with learning materials, calculated as total usage time divided by number of active users"
        ),

        // Percentage Metrics
        Indicator(
            name = "% Pass",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The proportion of students who successfully achieved passing grades on assessments or completed learning objectives successfully"
        ),
        Indicator(
            name = "% Fail",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The percentage of students who did not meet the minimum requirements for passing assessments or completing learning modules"
        ),
        Indicator(
            name = "Attendance %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The rate of student presence in classes or learning sessions compared to total scheduled sessions"
        ),
        Indicator(
            name = "Absence %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The percentage of missed classes or learning sessions out of total scheduled educational activities"
        ),
        Indicator(
            name = "Completion Rate",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The proportion of students who finished assigned courses, modules, or learning paths compared to those who started them"
        ),
        Indicator(
            name = "Completion per Assigned Task %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The percentage of successfully completed assignments, exercises, or learning activities out of all tasks assigned to students"
        ),
        Indicator(
            name = "Retention Rate / Content Revisited Rate",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The frequency at which learners return to review previously covered material, indicating knowledge reinforcement and long-term retention"
        ),
        Indicator(
            name = "Offline Usage % vs. Online",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The comparative usage of educational resources in offline mode versus online connectivity, showing accessibility patterns"
        ),

        // Count Metrics
        Indicator(
            name = "Score (Average)",
            type = YAxisTypes.COUNT.name,
            description = "The mean performance score across all assessments, tests, or evaluations for a given student group or time period"
        ),
        Indicator(
            name = "Score (Total)",
            type = YAxisTypes.COUNT.name,
            description = "The sum of all points earned by students across multiple assessments or cumulative performance measurements"
        ),

        Indicator(
            name = "Number of activities",
            type = YAxisTypes.COUNT.name,
            description = "The total count of learning exercises, assignments, quizzes, or educational tasks completed by students"
        ),
        Indicator(
            name = "Average Time Spent",
            type = YAxisTypes.COUNT.name,
            description = "The mean duration students dedicate to learning activities, measured in minutes per session or activity"
        ),
        Indicator(
            name = "Active Days per User",
            type = YAxisTypes.COUNT.name,
            description = "The average number of days each student engages with learning materials within a specific time period"
        ),

        // Top 5 Metrics
        Indicator(
            name = "Top 5: Apps",
            type = YAxisTypes.COUNT.name,
            description = "The five most frequently used educational applications based on user engagement time and activity frequency"
        ),
        Indicator(
            name = "Top 5: Languages",
            type = YAxisTypes.COUNT.name,
            description = "The five most commonly used languages for learning content consumption and interaction across the platform"
        ),
        Indicator(
            name = "Top 5: Lessons",
            type = YAxisTypes.COUNT.name,
            description = "The five most accessed and completed educational lessons based on student participation and completion rates"
        ),
        Indicator(
            name = "Top 5: Schools",
            type = YAxisTypes.COUNT.name,
            description = "The five educational institutions with the highest levels of student engagement, content usage, or academic performance"
        ),
        Indicator(
            name = "Top 5: Students",
            type = YAxisTypes.COUNT.name,
            description = "The five students demonstrating exceptional performance, high engagement levels, or outstanding academic achievement"
        )
    )
}