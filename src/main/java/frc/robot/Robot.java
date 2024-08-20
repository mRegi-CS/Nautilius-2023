// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.units.Velocity;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  /* This function is run when the robot is first started up and should be used for any initialization code*/
  //Este es el constructor del Robot =P
    //Instancia es cuando usas = new

  Timer timer = new Timer();

  /* +++++++++++++++++++++++++++++++++++ CHASIS +++++++++++++++++++++++++++++++++++++++ */
  //Declarar motores
  CANSparkMax motorLeftFront = new CANSparkMax(10, MotorType.kBrushless);
  CANSparkMax motorFrontRight = new CANSparkMax(13, MotorType.kBrushless);
  CANSparkMax motorBackLeft = new CANSparkMax(11, MotorType.kBrushless);
  CANSparkMax motorBackRight = new CANSparkMax(12, MotorType.kBrushless);

  //Declarar Encoders
  RelativeEncoder LeftEncoder = motorLeftFront.getEncoder();
  RelativeEncoder RightEncoder = motorFrontRight.getEncoder();

  //Declarar NavX
  AHRS navX = new AHRS(SPI.Port.kMXP);

  // CONTROLER 
  Joystick controlDriver = new Joystick(1);
  Joystick controlPlacer = new Joystick(2);

  //Differencial Drive
  DifferentialDrive chasis = new DifferentialDrive(motorLeftFront, motorFrontRight);

  /* ++++++++++++++++++++++++++++++++ INTAKE - SHOOTER +++++++++++++++++++++++++++++++++ */
  //Declarar Motores
  CANSparkMax motorIntShoBack = new CANSparkMax(3, MotorType.kBrushless);
  CANSparkMax motorIntShoFront = new CANSparkMax(4, MotorType.kBrushless);


  /* +++++++++++++++++++++++++++++++++++ PIVOTEO ++++++++++++++++++++++++++++++++++++++++ */
  //Declarar Motor
  CANSparkMax motorPivoteo = new CANSparkMax(5, MotorType.kBrushless);

  //Declarar Encoder
  RelativeEncoder PivoteoEncoder = motorPivoteo.getEncoder();

  //Limit Switch
  DigitalInput limitSwitch = new DigitalInput( 1);

  /* +++++++++++++++++++++++++++++++++++ VARIABLES +++++++++++++++++++++++++++++++++++++++++ */
  boolean isArcade = false;
  boolean McQueen = false;
  

  @Override
  public void robotInit() {
    motorBackLeft.follow(motorLeftFront);
    motorBackRight.follow(motorFrontRight);

    LeftEncoder.setPosition(0);
    RightEncoder.setPosition(0);
    PivoteoEncoder.setPosition(0);

    navX.reset();

    motorIntShoBack.setInverted(true);
    motorIntShoFront.setInverted(!motorIntShoFront.getInverted());

    motorIntShoBack.follow(motorIntShoFront);

  }

  @Override
  public void robotPeriodic() {

    // Para verificar (Driver) si esta en tanque o en Arcade
    SmartDashboard.putBoolean("Is Arcade?", isArcade);

    //Saber la posición en la que esta en Pivoteo (Driver), o para ver fallas (Driver y Progra)
    SmartDashboard.putNumber("Encoder Pivoteo / Position", PivoteoEncoder.getPosition());

    //
    SmartDashboard.putNumber("Left encoder / Motor", LeftEncoder.getPosition());
    SmartDashboard.putNumber("Right encoder / Motor", RightEncoder.getPosition());

  }

  @Override
  public void autonomousInit() {
    timer.start();

  }

  @Override
  public void autonomousPeriodic() {
    //Dispare, avanze, gire, avanze
    
    if (timer.get() < 5){
      goToPosition(135);//Acomoda el Pivoteo

      motorIntShoFront.set(0.3);//Dispara
      //Delay
      motorIntShoFront.set(0);//Para
    }

    if (timer.get() > 5 && timer.get() < 6){
      chasis.tankDrive(0.4,0.4);//Avanza
      //Delay
      chasis.tankDrive(0,0);//Para
    }
    
    if (timer.get() > 6 && timer.get() < 9.5 ){
      TurnRightAngle(0.3, 180);//Gira para regresar
    }
    
    if(timer.get() > 9.5 && timer.get() < 10.5){
      chasis.tankDrive(0.4,0.4);//Avanza/Regresa
      //Delay
      chasis.tankDrive(0,0);//Para
    }
    
    timer.stop();
  }

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {

    if (controlDriver.getRawButton(1)){
      isArcade = true;
    } else if (controlDriver.getRawButton(2)){
      isArcade = false;
    }


    if (isArcade) {
      //chasis.arcadeDrive(controlDriver.getRawAxis(1)*0.7,controlDriver.getRawAxis(2)*0.7);
      chasis.arcadeDrive(0.7,0.7);

    } else {
      chasis.tankDrive(controlDriver.getRawAxis(1)*0.7,controlDriver.getRawAxis(2)*0.7);

    }


    if (controlPlacer.getRawButton(1)){
      motorIntShoFront.set(0.5);
    } else if (controlPlacer.getRawButton(2)){
      motorIntShoFront.set(-0.5);
    } else {
      motorIntShoFront.set(0);
    }


    /* PIVOTEO CON ENCODERS */
    if (!limitSwitch.get()) {     //Si es verdadero
			motorPivoteo.set(0);    //Para
	  } else {    
		  if (controlPlacer.getRawButton(3)){
        goToPosition(50);
      } else if (controlPlacer.getRawButton(4)){
        goToPosition(100);
      } else {
        goToPosition(526);
      }

      if (controlPlacer.getRawButton(5)){
        motorPivoteo.set(0.3);
      } else if (controlPlacer.getRawButton(6)){
          motorPivoteo.set(-0.3);
      } else {
        motorPivoteo.set(0);
      }
      
    }
    
  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}

  /**
   * Esta función es para la SmartDashboard, es para que muestre datos importantes
   * @param palabra
   */
  public void functSmartDashboard(String palabra){

    SmartDashboard.putString("My Name", palabra);
    SmartDashboard.putString("My Team Num", palabra);
    SmartDashboard.putString("My Age", palabra);
    SmartDashboard.putString("Robot Name", palabra);
    SmartDashboard.putString("PRUEBA", palabra);
    SmartDashboard.putString("Similar", palabra);

  }


  /**
   * @return Esta función te permite establecer el pivoteo en la posición que necesitas
   * @param position Es el número de la posición en la que quieres que este el pivoteo
   */
  public void goToPosition(int position){

      while (!(PivoteoEncoder.getPosition() >= position+5 && PivoteoEncoder.getPosition() <= position-5)){
        motorPivoteo.set(0.3);
      }
      motorPivoteo.set(0);
  }

  /* ++++++++++++++++++++++++++++++ Funciones de Giro ++++++++++++++++++++++++++++ */

  /**
   * @return TurnLeft es una funcion hecha para que el robot gire a cierta velocidad a la izquierda
   * @param VelocityTurnLeft Es la velocidad en la que quieres que se haga el movimiento (En double)
   * Procedimiento:
   * Establece al motor izquierdo y derecho a la velocidad del argumento
   * Como es una funcion para girar a la izquierda al motor Izquierdo le establece la velocidad en negativo
   */
  public void TurnLeft(double VelocityTurnLeft){
    motorLeftFront.set(-VelocityTurnLeft);
    motorFrontRight.set(VelocityTurnLeft);
  }

  /**
   * @return TurnRight es una funcion hecha para que el robot gire a cierta velocidad a la derecha
   * @param VelocityTurnRight Es la velocidad en la que quieres que se haga el movimiento (En double)
   * Procedimiento:
   * Establece al motor izquierdo y derecho a la velocidad del argumento
   * Como es una funcion para girar a la derecha al motor derecho le establece la velocidad en negativo
   */
  public void TurnRight(double VelocityTurnRight){
    motorLeftFront.set(VelocityTurnRight); 
    motorFrontRight.set(-VelocityTurnRight);
  }

  /* ++++++++++++++++++++++++ Funciones de Giro con Angulo ++++++++++++++++++++++ */
  
  /**
   * @return TurnLeftAngle es una funcion hecha para que el robot gire a cierto angulo a la izquierda con cierta velocidad
   * @param VelTurnLeft Es la velocidad en que quieres que se haga el movimiento (En double)
   * @param ObjAngleLeft Es el Angulo en que quieres que gire el robot, como el objetivo (En double)
   * Procedimiento:
   * La funcion Utiliza un while, con la condicional idicando que se va a cumplir mientras tu angulo/navX sea menor a tu Objetivo/Angulo
   * Para girar Usa la funcion anterior de TurnLeft con la velocidad que le diste en el Argumento VelTurnLeft
   * Finalmente establece la velocidad de 0 de los motores / Los para
   */
  public void TurnLeftAngle(double VelTurnLeft, double ObjAngleLeft){
    while (navX.getAngle() < ObjAngleLeft){
      TurnLeft(VelTurnLeft);
    }
    motorLeftFront.set(0);
    motorFrontRight.set(0);
    
  }

  /**
   * @return TurnRightAngle es una funcion hecha para que el robot gire a cierto angulo a la derecha con cierta velocidad
   * @param VelTurnRight Es la velocidad en que quieres que se haga el movimiento (En double)
   * @param ObjAngleRight Es el Angulo en que quieres que gire el robot, como el objetivo (En double)
   * Procedimiento: 
   * La funcion Utiliza un while, con la condicional idicando que se va a cumplir mientras tu angulo/navX sea menor a tu Objetivo/Angulo
   * Para girar Usa la funcion anterior de TurnRight con la velocidad que le diste en el Argumento VelTurnRight
   * Finalmente establece la velocidad de 0 de los motores / Los para
   */
  public void TurnRightAngle(double VelTurnRight, double ObjAngleRight){
    while (navX.getAngle() < ObjAngleRight){
      TurnRight(VelTurnRight);
    }
    motorLeftFront.set(0);
    motorFrontRight.set(0);
  }

}
