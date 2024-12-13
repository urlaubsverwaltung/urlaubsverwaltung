package org.synyx.urlaubsverwaltung.extension.backup.restore;

/**
 * The ImportedIdTuple is used to store the id of an import and the id of the created application.
 *
 * @param idOfBackup  The database id of the backup
 * @param idOfRestore The database id of the restored entity
 */
public record ImportedIdTuple(Long idOfBackup, Long idOfRestore) {
}
