package frc.robot.subsystems.vision;

import java.util.Optional;

import org.littletonrobotics.junction.Logger;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.PoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import frc.robot.Constants;
import frc.robot.Robot;

public class VisionIOPhotonVision implements VisionIO {

    private final PhotonCamera camera;
    private final PhotonPoseEstimator photonEstimator;
    private double lastEstTimestamp = 0;

    private static final Pose3d[] cameraPoses;
    private static final String[] cameraIdentifiers;
    
    // Simulation
    private PhotonCameraSim cameraSim;
    private VisionSystemSim visionSim;

    // Camera locations
    static {
        switch (Constants.robot) {
            case "SIM":
                cameraPoses = 
                    /*
                    new Pose3d[] {
                        // Camera mounted on front of robot
                        new Pose3d(
                            Units.inchesToMeters(13), // 13 inches forward
                            Units.inchesToMeters(0), // 0 inches to the left
                            Units.inchesToMeters(20), // 3.75 inches off the ground
                            new Rotation3d(0, Units.degreesToRadians(-15), 0) // 15 degrees up
                        ),
                        // Camera mounted on the back of the robot
                        new Pose3d(
                            Units.inchesToMeters(-13), // 13 inches forward
                            Units.inchesToMeters(0), // 0 inches to the left
                            Units.inchesToMeters(5.75), // 3.75 inches off the ground
                            new Rotation3d(0, Units.degreesToRadians(-15), Units.degreesToRadians(180)) // 15 degrees up, 180 around
                        )
                    };
                    */
                    new Pose3d[] {
                        // Left Module
                        new Pose3d(
                            Units.inchesToMeters(20.75/2), // Forward
                            Units.inchesToMeters(20.75/2), // Left
                            Units.inchesToMeters(8.625),
                            new Rotation3d(0, Units.degreesToRadians(-15), Units.degreesToRadians(30))
                        ),
                        // Right Module
                        new Pose3d(
                            Units.inchesToMeters(20.75/2), // Forward
                            Units.inchesToMeters(-20.75/2), // Right
                            Units.inchesToMeters(8.625),
                            new Rotation3d(0, Units.degreesToRadians(-15), Units.degreesToRadians(-30))
                        ),
                        new Pose3d(
                            Units.inchesToMeters(11),
                            Units.inchesToMeters(0),
                            Units.inchesToMeters(24),
                            new Rotation3d(0, Units.degreesToRadians(-45), 0)
                        )
                    };
                cameraIdentifiers =
                    new String[] {
                        "FL-Module",
                        "FR-Module",
                        "Shooter"
                    };
                break;
            case "Real":
            cameraPoses = 
            new Pose3d[] {
                // Left Module
                new Pose3d(
                    Units.inchesToMeters(20.25/2), // Forward
                    Units.inchesToMeters(20.25/2), // Left
                    Units.inchesToMeters(8.625),
                    new Rotation3d(0, Units.degreesToRadians(-15), Units.degreesToRadians(30))
                ),
                // Right Module
                new Pose3d(
                    Units.inchesToMeters(20.25/2), // Forward
                    Units.inchesToMeters(-20.25/2), // Right
                    Units.inchesToMeters(8.625),
                    new Rotation3d(0, Units.degreesToRadians(-15), Units.degreesToRadians(-30))
                ),
                new Pose3d(
                    Units.inchesToMeters(12),
                    Units.inchesToMeters(0),
                    Units.inchesToMeters(22.5),
                    new Rotation3d(0, Units.degreesToRadians(-45), 0)
                )
                };
                cameraIdentifiers = new String[] {
                    "FL-Module",
                    "FR-Module",
                    "Shooter"
                };
                break;
            default:
                cameraPoses = new Pose3d[] {};
                cameraIdentifiers = new String[] {};
                break;
        }
    }

    public VisionIOPhotonVision(String identifier, int instance) {
        System.out.println("[Init] Creating VisionIOPhotonVision " + identifier + " with camera (" + cameraIdentifiers[instance] + ")");
        camera = new PhotonCamera(cameraIdentifiers[instance]);

        photonEstimator = 
            //new PhotonPoseEstimator(AprilTagFields.k2024Crescendo.loadAprilTagLayoutField(), PoseStrategy.MULTI_TAG_PNP_ON_RIO, new Transform3d(new Pose3d(), cameraPoses[Instance]));
            new PhotonPoseEstimator(AprilTagFields.k2024Crescendo.loadAprilTagLayoutField(), PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, camera, new Transform3d(new Pose3d(), cameraPoses[instance]));
        photonEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);

        // Simulation
        if (Robot.isSimulation()) {
            visionSim = new VisionSystemSim("main");
            visionSim.addAprilTags(AprilTagFields.k2024Crescendo.loadAprilTagLayoutField());

            // Simulated Camera Properties
            var cameraProp = new SimCameraProperties();
            cameraProp.setCalibration(1280, 800, Rotation2d.fromDegrees(70));
            cameraProp.setCalibError(0.35, 0.10);
            cameraProp.setFPS(15);
            cameraProp.setAvgLatencyMs(30);
            cameraProp.setLatencyStdDevMs(5);

            // Create PhotonCameraSim to update the linked PhotonCamera with visible targets
            cameraSim = new PhotonCameraSim(camera, cameraProp);

            // Add the simulated camera to view the targets on the simulated field
            visionSim.addCamera(cameraSim, new Transform3d(new Pose3d(), cameraPoses[instance]));

            cameraSim.enableDrawWireframe(true);
        }
    }

    public PhotonPipelineResult getLatestResult() {
        return camera.getLatestResult();
    }

    /** 
     * The latest estimated robot pose on the field based on this vision data. This may be empty.
     * 
     * @return An {@link EstimatedRobotPose} with an estimated pose, estimate timestamp, and targets used for estimation.
     */
    public Optional<EstimatedRobotPose> getEstimatedGlobalPose() {
        var visionEst = photonEstimator.update();
        double latestTimestamp = camera.getLatestResult().getTimestampSeconds();
        boolean newResult = Math.abs(latestTimestamp - lastEstTimestamp) > 1e-5;
        if (Robot.isSimulation()) {
            visionEst.ifPresentOrElse(
                est ->
                    getSimDebugField()
                        .getObject("VisionEstimation")
                        .setPose(est.estimatedPose.toPose2d()),
                () -> {
                    if (newResult) getSimDebugField().getObject("VisionEstimation").setPoses();
                });
        }
        if (newResult) lastEstTimestamp = latestTimestamp;
        return visionEst;
    }

    /**
     * The standard deviations of the estimated pose from {@link #getEstimatedGlobalPose()}, for use
     * with a PoseEstimator.
     * This should only be used when there are targets visible.
     * 
     * @param estimatedPose The estimated pose to guess standard deviations for.
     */
    public Matrix<N3, N1> getEstimationStdDevs(Pose2d estimatedPose) {
        var estStdDevs = VecBuilder.fill(4, 4, 8); // Generic StdDevs for single tag usage
        var targets = getLatestResult().getTargets();
        int numTags = 0;
        double avgDist = 0;
        for (var tgt : targets) {
            var tagPose = photonEstimator.getFieldTags().getTagPose(tgt.getFiducialId());
            if (tagPose.isEmpty()) continue;
            numTags++;
            avgDist += tagPose.get().toPose2d().getTranslation().getDistance(estimatedPose.getTranslation());
        }
        if (numTags == 0) return estStdDevs; // Return default values if no tags are visible
        avgDist /= numTags;
        // Decrease StdDevs if multiple targets are visible
        if (numTags > 1) estStdDevs = VecBuilder.fill(0.5, 0.5, 1); // Generic StdDevs for multitag
        // Increase StdDevs based on (average) distance
        if (numTags == 1 && avgDist > 4) 
            estStdDevs = VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE); // If single tag + 4 meters away we probably shouldnt be using this pose data
        else estStdDevs = estStdDevs.times(1 + (avgDist * avgDist / 30));

        return estStdDevs;
    }

    // Simulation code
    
    public void simulationPeriodic(Pose2d robotSimPose) {
        visionSim.update(robotSimPose);
    }

    public void resetSimPose(Pose2d pose) {
        if (Robot.isSimulation()) visionSim.resetRobotPose(pose);
    }

    public Field2d getSimDebugField() {
        if (!Robot.isSimulation()) return null;
        return visionSim.getDebugField();
    }

    @Override
    public void updateInputs(VisionIOInputs inputs) {
        if (Robot.isSimulation()) {
            visionSim.update(inputs.robotPose);
        }

        inputs.tagCount = getLatestResult().getTargets().size();
        var poseEst = getEstimatedGlobalPose();
        poseEst.ifPresentOrElse(
            est -> {
                inputs.estimatedPose = est.estimatedPose;
                inputs.stdDevs = getEstimationStdDevs(est.estimatedPose.toPose2d());
                //inputs.timestamp = Units.secondsToMilliseconds(est.timestampSeconds);
                inputs.timestamp = est.timestampSeconds;
            },
            () -> {
                inputs.estimatedPose = null;
                inputs.stdDevs = VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            });
    }
}
