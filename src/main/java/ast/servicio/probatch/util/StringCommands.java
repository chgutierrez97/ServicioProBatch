package ast.servicio.probatch.util;

public enum StringCommands {
    IMPERSONALIZATION_USER {
        @Override
        public String toString() {
            return "su";
        }
    },

    IMPERSONALIZATION_USER_FULL {
        @Override
        public String toString() {
            return "/bin/su";
        }
    },


    HYPHEN {
        @Override
        public String toString() { return " - "; }
    },

    COMMAND_MAIN {
        @Override
        public String toString() {
            return "/bin/sh";
        }
    },

    COMMAND_OPTION {
        @Override
        public String toString() { return "-c"; }
    },

    EXEC {
        @Override
        public String toString() { return "exec "; }
    },

    EXPORT {
        @Override
        public String toString() { return "export "; }
    },

    SYBASEENV {
        @Override
        public String toString() { return ". /sybase/SYBASE.sh; DSQUERY=SYBFACE;export DSQUERY ; . /sybase/setenv.sh "; }
    },

    PUNTO_COMA {
        @Override
        public String toString() { return "; "; }
    },
    STRING_EMPTY {
        @Override
        public String toString() { return " "; }

    },
    QUOTE_SIMPLE {
        @Override
        public String toString() { return "'"; }
    },

    CHDIR {
        @Override
        public String toString() { return "cd"; }
    }
}
