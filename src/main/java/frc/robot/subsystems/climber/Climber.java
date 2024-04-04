package frc.robot.subsystems.climber;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.Constants.ClimberConstants;

public class Climber extends SubsystemBase {
    private final double upSpeed = 0.75;
    private final double downSpeed = -0.75;
    private final double adjustmentSpeed = 0.25;

    private boolean autoUp = false;
    private boolean autoDown = true;
    private boolean autoAdjust = false;

    private final ClimberIO io;
    private final GyroIO gyro;
    private final ClimberIOInputsAutoLogged inputs = new ClimberIOInputsAutoLogged();

    /// TTHIS IS WORK IN PROGRESS - WE NEED TO UPDATE HOW CLIMBER USES THESE CONTROLERS DURING PERIODIC()
    // AND UPDATE ALL THE COMMANDS TO USE SetGoal AND 
    //private final ProfiledPIDController leftController = new ProfiledPIDController(0, 0, 0, 
    // new Constraints(null, null));
    //private final ProfiledPIDController rightController = new ProfiledPIDController(0, 0, 0,
    // new Constraints(null, null));


    public Climber(ClimberIO io, GyroIO gyro) {
        this.io = io;
        this.gyro = gyro;

        setDefaultCommand(
                run(() -> {
                    io.setLeftPercent(0.0);
                    io.setRightPercent(0.0);
                }));
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Launcher", inputs);

        // Need to check this - is this going to work? Compare with how Module does it.
        //io.setRightVoltage(rightController.calculate(io.getRightPosition()));
        //io.setLeftVoltage(leftController.calculate(io.getLeftPosition()));
    }

    /** command to climb autonomously once in position */
    public Command autoClimb() {
        return (runOnce(() -> {
            if (autoUp == false && autoDown == false && autoAdjust == true) {
                io.setLeftPosition(ClimberConstants.maxPosition);
                io.setRightPosition(ClimberConstants.maxPosition);

                autoUp = true;
                autoDown = false;
                autoAdjust = false;
            } else if (autoUp == true && autoDown == false && autoAdjust == false) {
                io.setLeftPosition(ClimberConstants.minimumElevation);
                io.setRightPosition(ClimberConstants.minimumElevation);

                autoUp = false;
                autoDown = true;
                autoAdjust = false;
            } else {
                autoClimbAdjustmentCommand();

                autoUp = false;
                autoDown = false;
                autoAdjust = true;
            }
        }));
    }

    /** automatically balances the robot while hanging */
    public Command autoClimbAdjustmentCommand() {
        return Commands.run(() -> {
            if (gyro.getLeftRightAngle() < 0) {
                io.setLeftPercent(adjustmentSpeed);
            } else if (gyro.getLeftRightAngle() > 0) {
                io.setRightPercent(adjustmentSpeed);
            } else {
                Commands.print("Climb Successful, continuing to balance");
                io.setLeftPercent(0.0);
                io.setRightPercent(0.0);
            }
        });
    }

    /** sets the upward voltage to both arms */
    public Command upBothCommand() {
        return Commands.run(
                () -> {
                    io.setLeftPercent(upSpeed);
                    io.setRightPercent(upSpeed);
                });
    }

    /** sets the upward voltage to the left arm only */
    public Command upLeftCommand() {
        return Commands.run(
                () -> {
                    io.setLeftPercent(upSpeed);
                });
    }

    /** sets the upward voltage to the right arm only */
    public Command upRightCommand() {
        return Commands.run(
                () -> {
                    io.setRightPercent(upSpeed);
                });
    }

    /** sets the downward voltage to both arms */
    public Command downBothCommand() {
        return Commands.run(
                () -> {
                    io.setLeftPercent(downSpeed);
                    io.setRightPercent(downSpeed);
                });
    }

    /** sets the downward voltage to the left arm only */
    public Command downLeftCommand() {
        return Commands.run(
                () -> {
                    io.setLeftPercent(downSpeed);
                });
    }

    /** sets the downward voltage to the right arm only */
    public Command downRightCommand() {
        return Commands.run(
                () -> {
                    io.setRightPercent(downSpeed);
                });
    }
}
