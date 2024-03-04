package frc.robot;


public class Constants {
    //public static final String robot = "SIM";
    public static final String robot = "Real";

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

        public static int intakeMotorID = 14;
        public static int intakeLimitID = 0;
        public static double rawIntakeSpeedLimiter = 0.25;

        public static int pivotMotorID = 15;
        public static int pivotEncoderID = 1;
        public static int pivotGearRatio = 80;
        public static double pivotMaxAngleRad = 2.6;  //.48 is absolute max
        public static double pivotMinAngleRad = 0.25;  // .02 is absolute min
        public static double rawPivotSpeedLimiter = 0.25;

        public static double kP = 5;
        public static double kD = 1.5;
        public static double kMaxVelocityRadPerSecond = 6.28;
        public static double kMaxAccelerationRadPerSecSquared = 3.14*0.75;
        public static int kEncoderDistancePerPulse = 2048;
        public static int kArmOffsetRads = 0;
        public static double encoderZeroOffset = -0.203;

        public static double kSVolts = 1;
        public static double kGVolts = 1;
        public static double kVVoltSecondPerRad = 0.5;
        public static double kAVoltSecondSquaredPerRad = 0.1;


    }

    public static class LaunchConstants {
        public static int leftMotorID = 16;
        public static int rightMotorID = 17;

    }
}
