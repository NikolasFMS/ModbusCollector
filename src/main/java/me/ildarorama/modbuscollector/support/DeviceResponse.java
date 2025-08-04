package me.ildarorama.modbuscollector.support;

import java.time.LocalDateTime;

public class DeviceResponse {

    private LocalDateTime timestamp;
    private float rotorSpeed; // Частота вращения ротора (n), об/мин
    private float oilTemperature; // Температура масла (Tм), °C
    private float exhaustGasTemp1; // Температура ОГ на входе в турбину (Tог1), °C
    private float exhaustGasTemp2; // Температура ОГ на выходе из турбины (Tог2), °C
    private float airTemp1; // Температура воздуха на входе в компрессор (Tв1), °C
    private float airTemp2; // Температура воздуха на выходе из компрессора (Tв2), °C
    private float gasPressure1; // Избыточное давление газа на входе в турбину (Pг1), кПа
    private float gasPressure2; // Избыточное давление газа на выходе из турбины (Pг2), кПа
    private float compressorPressure1; // Разрежение на входе в компрессор (Pк1), кПа
    private float compressorPressure2; // Избыточное давление на выходе из компрессора (Pк2), кПа

    // Поля для совместимости с ObjectPropertyBinding
    private float a1; //  rotorSpeed
    private float a2; //  oilTemperature
    private float a3; //  exhaustGasTemp1
    private float a4; //  exhaustGasTemp2
    private float a5; //  airTemp1
    private float a6; //  airTemp2
    private float a7; //  gasPressure1
    private float a8; //  gasPressure2
    private float a9; //  compressorPressure1
    private float a10; //  compressorPressure2

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public float getRotorSpeed() {
        return rotorSpeed;
    }

    public void setRotorSpeed(float rotorSpeed) {
        this.rotorSpeed = rotorSpeed;
    }

    public float getOilTemperature() {
        return oilTemperature;
    }

    public void setOilTemperature(float oilTemperature) {
        this.oilTemperature = oilTemperature;
    }

    public float getExhaustGasTemp1() {
        return exhaustGasTemp1;
    }

    public void setExhaustGasTemp1(float exhaustGasTemp1) {
        this.exhaustGasTemp1 = exhaustGasTemp1;
    }

    public float getExhaustGasTemp2() {
        return exhaustGasTemp2;
    }

    public void setExhaustGasTemp2(float exhaustGasTemp2) {
        this.exhaustGasTemp2 = exhaustGasTemp2;
    }

    public float getAirTemp1() {
        return airTemp1;
    }

    public void setAirTemp1(float airTemp1) {
        this.airTemp1 = airTemp1;
    }

    public float getAirTemp2() {
        return airTemp2;
    }

    public void setAirTemp2(float airTemp2) {
        this.airTemp2 = airTemp2;
    }

    public float getGasPressure1() {
        return gasPressure1;
    }

    public void setGasPressure1(float gasPressure1) {
        this.gasPressure1 = gasPressure1;
    }

    public float getGasPressure2() {
        return gasPressure2;
    }

    public void setGasPressure2(float gasPressure2) {
        this.gasPressure2 = gasPressure2;
    }

    public float getCompressorPressure1() {
        return compressorPressure1;
    }

    public void setCompressorPressure1(float compressorPressure1) {
        this.compressorPressure1 = compressorPressure1;
    }

    public float getCompressorPressure2() {
        return compressorPressure2;
    }

    public void setCompressorPressure2(float compressorPressure2) {
        this.compressorPressure2 = compressorPressure2;
    }

    public float getA1() {
        return a1;
    }

    public void setA1(float value) {
        this.a1 = value;
        this.rotorSpeed = value;
    }

    public float getA2() {
        return a2;
    }

    public void setA2(float value) {
        this.a2 = value;
        this.oilTemperature = value;
    }

    public float getA3() {
        return a3;
    }

    public void setA3(float value) {
        this.a3 = value;
        this.exhaustGasTemp1 = value;
    }

    public float getA4() {
        return a4;
    }

    public void setA4(float value) {
        this.a4 = value;
        this.exhaustGasTemp2 = value;
    }

    public float getA5() {
        return a5;
    }

    public void setA5(float value) {
        this.a5 = value;
        this.airTemp1 = value;
    }

    public float getA6() {
        return a6;
    }

    public void setA6(float value) {
        this.a6 = value;
        this.airTemp2 = value;
    }

    public float getA7() {
        return a7;
    }

    public void setA7(float value) {
        this.a7 = value;
        this.gasPressure1 = value;
    }

    public float getA8() {
        return a8;
    }

    public void setA8(float value) {
        this.a8 = value;
        this.gasPressure2 = value;
    }

    public float getA9() {
        return a9;
    }

    public void setA9(float value) {
        this.a9 = value;
        this.compressorPressure1 = value;
    }

    public float getA10() {
        return a10;
    }

    public void setA10(float value) {
        this.a10 = value;
        this.compressorPressure2 = value;
    }
}
