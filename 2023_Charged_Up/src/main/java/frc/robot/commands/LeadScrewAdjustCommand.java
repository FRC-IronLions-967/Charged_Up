package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.SubsystemsInstance;

public class LeadScrewAdjustCommand extends CommandBase {
  
  private double speed;
  private SubsystemsInstance inst;

  public LeadScrewAdjustCommand(double speed) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.speed = speed;

    inst = SubsystemsInstance.getInstance();
    addRequirements(inst.leadScrewSubsystem);

  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    inst.leadScrewSubsystem.runMotor(speed);

  }


  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return true;
  }
}