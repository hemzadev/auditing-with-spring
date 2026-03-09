package ma.hemzastudio.bestlogspractices.common.audit.enums;

public enum AuditAction {

    /** Entity was first persisted. */
    CREATE,

    /** One or more fields were modified. */
    UPDATE,

    /** Soft-deleted (deleted = true, row still in DB). */
    DELETE,

    /** Soft-delete reversed (deleted = false). */
    RESTORE,

    /** Status transition (e.g. ACTIVE → SUSPENDED). */
    STATUS_CHANGE,

    /** Permission or role change. */
    PERMISSION_CHANGE

}
