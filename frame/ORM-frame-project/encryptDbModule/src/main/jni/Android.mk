LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    jni_exception.cpp \
	org_spatialite_database_SQLiteCompiledSql.cpp \
	org_spatialite_database_SQLiteDatabase.cpp \
	org_spatialite_database_SQLiteProgram.cpp \
	org_spatialite_database_SQLiteQuery.cpp \
	org_spatialite_database_SQLiteStatement.cpp \
	org_spatialite_CursorWindow.cpp \
	CursorWindow.cpp \
  jniproj.c \
  posix.c \
  exidx_workaround.cpp
#	org_spatialite_database_SQLiteDebug.cpp

LOCAL_STATIC_LIBRARIES := \
    sqlite \
	spatialite \
    freexl

LOCAL_CFLAGS += -DLOG_NDEBUG \
	    -DSQLITE_HAS_CODEC \
            -DSQLITE_SOUNDEX \
            -DHAVE_USLEEP=1 \
            -DSQLITE_MAX_VARIABLE_NUMBER=99999 \
            -DSQLITE_TEMP_STORE=3 \
            -DSQLITE_THREADSAFE=1 \
            -DSQLITE_DEFAULT_JOURNAL_SIZE_LIMIT=1048576 \
            -DNDEBUG=1 \
            -DSQLITE_ENABLE_MEMORY_MANAGEMENT=1 \
            -DSQLITE_ENABLE_LOAD_EXTENSION \
            -DSQLITE_ENABLE_COLUMN_METADATA \
            -DSQLITE_ENABLE_UNLOCK_NOTIFY \
            -DSQLITE_ENABLE_RTREE \
            -DSQLITE_ENABLE_STAT3 \
            -DSQLITE_ENABLE_STAT4 \
            -DSQLITE_ENABLE_JSON1 \
            -DSQLITE_ENABLE_FTS3_PARENTHESIS \
            -DSQLITE_ENABLE_FTS4 \
            -DSQLITE_ENABLE_FTS5 \
            -DSQLCIPHER_CRYPTO_OPENSSL \

# libs from the NDK
LOCAL_LDLIBS += -llog -latomic

LOCAL_LDFLAGS += -fuse-ld=bfd

LOCAL_MODULE:= libandroid_spatialite

include $(BUILD_SHARED_LIBRARY)

$(call import-module,proj.4)
$(call import-module,geos)
$(call import-module,libiconv)
$(call import-module,libxml2)
$(call import-module,liblzma)
$(call import-module,freexl)
$(call import-module,sqlite)
$(call import-module,libspatialite)

# NOTE: iconv is dependency of Spatialite virtual modules like VirtualText, VirtualShape, VirtualXL, etc. 
