package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkMaxLimitSwitch;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxLimitSwitch.Type;

import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.commands.LeadScrewInitializeCommand;

public class LeadScrewSubsystem extends SubsystemBase {
    
    private CANSparkMax leadScrew;
    private SparkMaxLimitSwitch screwForwardLimit;
    private SparkMaxLimitSwitch screwReverseLimit;
    private SparkMaxPIDController leadScrewController;

    private Boolean leadScrewInitialized;
    private static LeadScrewStates state = LeadScrewStates.UNINITIALIZED;

    private double mmPerInch = 25.4;
    private double leadScrewPitch = 8; //2mm pitch, 4 start, 8mm per revolution
    private double leadScrewRevPerInch = mmPerInch / leadScrewPitch; //3.175 revolution for 1 inch approx.
    private double leadScrewGearboxRatio = 9.0; //modify as hardware changes, current ratio is 9:1

    public LeadScrewSubsystem() {
        leadScrew = new CANSparkMax(5, MotorType.kBrushless);
        leadScrew.setInverted(true);

        screwForwardLimit = leadScrew.getForwardLimitSwitch(Type.kNormallyOpen);
        screwReverseLimit = leadScrew.getReverseLimitSwitch(Type.kNormallyOpen);

        leadScrew.getEncoder().setPosition(0.0);
        leadScrew.getEncoder().setPositionConversionFactor(leadScrewGearboxRatio * leadScrewRevPerInch);
        leadScrew.setClosedLoopRampRate(1.0);

        leadScrewController = leadScrew.getPIDController();
        leadScrewController.setP(1);  //needs tuning
        leadScrewController.setI(0);
        leadScrewController.setD(0);
        leadScrewController.setReference(0, ControlType.kPosition);

        leadScrewInitialized = false;

    }

    public void initializeLeadScrew() {
        if (leadScrewInitialized) return;
        while (!screwReverseLimit.isPressed()) {
            leadScrew.set(-0.3);
        }
        leadScrew.set(0.0);
        leadScrew.getEncoder().setPosition(0.0);
        leadScrewController.setReference(0, ControlType.kPosition);

        screwForwardLimit.enableLimitSwitch(true);
        screwReverseLimit.enableLimitSwitch(true);

        leadScrewInitialized = true;
        state = LeadScrewStates.MANUAL;
        System.out.println("Lead Screw Initialized");
    }

    public void runMotor(double speed) {
        if (state != LeadScrewStates.UNINITIALIZED) {
            state = LeadScrewStates.MANUAL;
            leadScrewController.setPositionPIDWrappingEnabled(false);
            leadScrew.set(speed);
        }
    }

    /**
     * sets how far the lead screw should be extended
     * @param position in inches
     */
    public void setLeadScrewPosition(double position) {
        if (state != LeadScrewStates.UNINITIALIZED) {
            state = LeadScrewStates.AUTO;
            leadScrewController.setPositionPIDWrappingEnabled(true);
            leadScrewController.setReference(position, ControlType.kPosition);
        }
    }

    public void stopLeadScrew() {
        leadScrewController.setReference(leadScrew.getEncoder().getPosition(), ControlType.kPosition);
        leadScrewController.setPositionPIDWrappingEnabled(true);
        
    }

    @Override
    public void periodic() {
        switch(state) {
            case UNINITIALIZED:
                if (!leadScrewInitialized) {
                    state = LeadScrewStates.INITIALIZING;
                } else {
                    state = LeadScrewStates.MANUAL;
                }
                break;
            case INITIALIZING:
                CommandScheduler.getInstance().schedule(new LeadScrewInitializeCommand());
                break;
            case MANUAL:
                break;
            case AUTO:
                break;
        }
    }
}
