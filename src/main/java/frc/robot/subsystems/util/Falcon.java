package frc.robot.subsystems.util;

import java.util.ArrayList;

import javax.swing.text.Highlighter.Highlight;

import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.kOI;
import frc.robot.OI;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

public class Falcon extends Motor {
    WPI_TalonFX motor;
    public int canID;
    public double currentLimit;
    private double startingEncoder;
    private double endingEncoder;

    public Falcon(WPI_TalonFX motor) {
        this.motor = motor;
        this.oi = OI.getInstance();
        this.canID = motor.getDeviceID();
        this.currentLimit = motor.getSupplyCurrent();
    }

    @Override
    public void startTwitch() {
        startingEncoder = motor.getSelectedSensorPosition();
        setSpeed(.1, false);
    }

    @Override
    public void endTwitch() {
        disable();
        endingEncoder = motor.getSelectedSensorPosition();
    }

    @Override
    public boolean checkEncoderErrors() {
        if (endingEncoder > startingEncoder + 40) {
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<String> findErrors() {
        checkElecErrors();
        return getErrors();
    }

    @Override
    public ArrayList<String> findTotalErrors() {
        ArrayList<String> output = findErrors();
        if (!checkEncoderErrors()) {
            output.add("The motor isn't working");
        }
        return output;
    }

    @Override
    public String getRegisterString(String subsystem, String swerveName) {
        return subsystem + " " + swerveName + " " + this.canID + " 0 " + "0.0 " + "0 " + ((motor.getInverted()) ? 1 : 0)
                + " 0 " + this.currentLimit + " " + this.lowLimit + " " + this.highLimit + " 0 " + "0";
    }

    @Override
    public void setIdle(IdleMode idle) {
        if (idle == IdleMode.COAST) {
            motor.setNeutralMode(NeutralMode.Coast);
        } else {
            motor.setNeutralMode(NeutralMode.Brake);
        }
    }

    @Override
    public void invertMotor(boolean flipped) {
        motor.setInverted(flipped);
    }

    @Override
    public void setSpeed(double speed, boolean isJoystick) {

        double newSpeed;
        if (isJoystick) {
            newSpeed = oi.getDriveTrainTranslationY();
            if (newSpeed < .08 && newSpeed > -.08) {
                newSpeed = 0;
            }
        } else {
            newSpeed = speed;
        }

        double position = motor.getSelectedSensorPosition();

        if (((newSpeed > 0 && position >= highLimit) || (newSpeed < 0 && position <= lowLimit))) {
            newSpeed = 0;
        }

        motor.set(newSpeed);
    }

    @Override
    public void setCurrentLimit(double currentLimit) {
        SupplyCurrentLimitConfiguration CurrentLimit = new SupplyCurrentLimitConfiguration(true, currentLimit,
                currentLimit, 0.1);
        motor.configSupplyCurrentLimit(CurrentLimit);
    }

    @Override
    public void setEncoderLimit(double lowLimit, double highLimit) {
        if (lowLimit == -2) {
            this.lowLimit = Double.MAX_VALUE * -1;
        } else {
            this.lowLimit = lowLimit * 1000;
        }

        if (highLimit == 0) {
            this.highLimit = Double.MAX_VALUE;
        } else {
            this.highLimit = highLimit * 1000;
        }
    }

    @Override
    public void disable() {
        motor.disable();
    }

    public void checkElecErrors() {
    }

    public ArrayList<String> getErrors() {
        return null;
    }
}
