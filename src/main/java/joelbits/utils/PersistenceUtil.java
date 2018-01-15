package joelbits.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class PersistenceUtil {
    private static final Logger log = LoggerFactory.getLogger(PersistenceUtil.class);
    private static Connection connection;
    private static Configuration configuration;
    private static String TABLE_CHANGEDFILE = "ChangedFile";
    private static String TABLE_PROJECT = "Project";
    private static String COLUMN_FAMILY_PROJECT = "metadata";
    private static String COLUMN_FAMILY_CHANGEDFILE = "changed_files";

    static {
        try {
            configuration = HBaseConfiguration.create();
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    public static boolean tableExists(String table) {
        try {
            return MetaTableAccessor.tableExists(ConnectionFactory.createConnection(HBaseConfiguration.create()), TableName.valueOf(table));
        } catch (IOException e) {
            log.error(e.toString(), e);
        }

        return false;
    }

    /**
     * The map containing the projects (Project protobuf) has a key corresponding to the url of the project
     * and a binary representation of the project's metadata as value.
     *
     * @param projects       A map of the projects to persist.
     */
    public static void persistProjects(Map<String, byte[]> projects){
        try {
            Table table = connection.getTable(TableName.valueOf(TABLE_PROJECT));
            for (Map.Entry<String, byte[]> project : projects.entrySet()) {
                Put key = new Put(Bytes.toBytes(project.getKey()));
                key.addColumn(Bytes.toBytes(COLUMN_FAMILY_PROJECT), Bytes.toBytes(COLUMN_FAMILY_PROJECT), project.getValue());
                table.put(key);
            }
            table.close();
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    /**
     * The map containing the changed files (ASTRoot Protobuf) has a key corresponding to the file path of the
     * changed file (ASTRoot Protobuf) which is used as the column name of each file.
     *
     * @param changedFiles      A map of url:revisions with list of the changed files to persist.
     */
    public static void persistChangedFiles(Map<String, Map<String, byte[]>> changedFiles) {
        try {
            Table table = connection.getTable(TableName.valueOf(TABLE_CHANGEDFILE));
            for (Map.Entry<String, Map<String, byte[]>> revision : changedFiles.entrySet()) {
                Put files = new Put(Bytes.toBytes(revision.getKey()));
                for (Map.Entry<String, byte[]> changedFile : revision.getValue().entrySet()) {
                    files.addColumn(Bytes.toBytes(COLUMN_FAMILY_CHANGEDFILE), Bytes.toBytes(changedFile.getKey()), changedFile.getValue());
                }
                table.put(files);
            }
            table.close();
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    public static void createProjectTable() {
        try {
            createTable(TABLE_PROJECT, "url", "metadata");

            log.info("Table " + TABLE_PROJECT + " created Successfully");
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    private static void createTable(String tableName, String key, String values) throws IOException {
        Admin admin = connection.getAdmin();

        HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
        tableDescriptor.addFamily(new HColumnDescriptor(key));
        tableDescriptor.addFamily(new HColumnDescriptor(values));

        admin.createTable(tableDescriptor);
        admin.close();
    }

    public static void createChangedFileTable() {
        try {
            createTable(TABLE_CHANGEDFILE,"url_revision","changed_files");

            log.info("Table " + TABLE_CHANGEDFILE + " created Successfully");
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    public static void closeConnection() {
        try {
            connection.close();
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }
}
