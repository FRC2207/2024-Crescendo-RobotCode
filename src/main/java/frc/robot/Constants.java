package frc.robot;

public class Constants {
    public static final String robot = "SIM";
    //public static final String robot = "Real";

    // Is tunable constant. Enables SysId routines in the autonomous selectable chooser.
    public static final boolean isTuningMode = true;

    public static class SwerveConstants {
        public static class Modules {
            public static class FrontLeft {
                public static int drive = 1;
                public static int turn = 2;
                public static int encoder = 9;
                public static double encoderOffset = 0.0;
            }
            public static class FrontRight {
                public static int drive = 3;
                public static int turn = 4;
                public static int encoder = 10;
                public static double encoderOffset = 0.0;
            }
            public static class BackLeft {
                public static int drive = 5;
                public static int turn = 6;
                public static int encoder = 11;
                public static double encoderOffset = 0.0;
            }
            public static class BackRight {
                public static int drive = 7;
                public static int turn = 8;
                public static int encoder = 12;
                public static double encoderOffset = 0.0;
            }
        }
    }

    public static class IntakeConstants {
        public static int gearRatio = 16;
        public static int intakeMotorID = 14;
        public static int pivotMotorID = 15;
    }

    public static class LaunchConstants {
        public static int launchMotorID = 16;
        public static int followMotorID = 17;
    }
}
