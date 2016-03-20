function setSteeringVec(vector){
    @RAW LOCK STEERING TO vector.
}

function setSteeringPYR(pitch, yaw, roll){
    setSteeringVec(R(pitch, yaw, roll));
}

function setThrottle(throt){
    @RAW LOCK THROTTLE TO throt.
}

function getMaxThrust(){
    @RAW RETURN SHIP:MAXTHRUST.
}

function getAltitude(){
    @RAW RETURN SHIP:ALTITUDE.
}

function getThrottle(){
    @RAW RETURN THROTTLE.
}

function clear(){
    @RAW CLEARSCREEN.
}

function doStage(){
    @RAW STAGE.
}

function getApoapsis(){
    @RAW RETURN SHIP:APOAPSIS.
}

function getPeriapsis(){
    @RAW RETURN SHIP:PERIAPSIS.
}

function getSurfaceVelocity(){
    @RAW RETURN SHIP:VELOCITY:SURFACE:MAG.
}

function printXY(text, x, y){
    @RAW PRINT text AT (x, y).
}

function getCurrentStageFuel(){
    @RAW RETURN STAGE:LIQUIDFUEL.
}

function getCurrentStageSolidFuel(){
    @RAW RETURN STAGE:SOLIDFUEL;
}