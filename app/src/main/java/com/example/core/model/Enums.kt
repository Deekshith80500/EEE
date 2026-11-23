package com.example.core.model

enum class ParticipationStatus(val label: String) {
  PARTICIPATED("Participated"),
  DID_NOT_PARTICIPATE("Did not participate"),
  NOT_RECORDED("Not recorded")
}

enum class SourceType(val label: String) {
  USER_ENTERED("User Entered"),
  IMPORTED("Imported"),
  SYNCED("Synced"),
  SYSTEM_DERIVED("System Derived"),
  DEMO("Demo Data")
}

enum class ActivityStatus(val label: String) {
  DRAFT("Draft"),
  SCHEDULED("Scheduled"),
  ACTIVE("Active"),
  COMPLETED("Completed")
}

enum class AssessmentType(val label: String) {
  PRE_ACTIVITY("Pre-Activity"),
  POST_ACTIVITY("Post-Activity")
}

enum class SyncState(val label: String) {
  LOCAL_ONLY("Local mode"),
  PENDING("Pending sync"),
  SYNCED("Synced"),
  FAILED("Sync failed")
}
