@Include "res:///kos_jebscript.js"
@Include "res:///kos_adv.js"
@Include "res:///kos_toolkit.js"
//@include "res:///kos_kslib_github.js"

var apo = 0;
var peri = 0;
var vel = 0;
var fuel = 0;
var lfuel = 0;
var state = 0;
var altitude = 0;
var aposTarget = 0;

function updateScreen() {
    apo = getApoapsis();
    peri = getPeriapsis();
    vel = getSurfaceVelocity();
    fuel = getCurrentStageSolidFuel();
    lfuel = getCurrentStageFuel();
    altitude = getAltitude();
    printXY("APO: " + apo, 2, 1);
    printXY("PERI: " + peri, 2, 2);
    printXY("VEL: " + vel, 2, 3);
    printXY("ALT: " + altitude, 2, 4);
    printXY("SFUEL: " + fuel, 2, 6);
    printXY("LFUEL: " + lfuel, 2, 7);
    printXY("APOS: " + ETA:APOAPSIS, 2, 8);
}

function badclear() {
    var q = 0;
    while (q < 32) {
        print(" ");
        q++;
    }
}

function main() {
    @RAW LOCK STEERING TO UP.
    setThrottle(0.7);
    badclear();
    doStage();
    while (1) {
        updateScreen();
        if(state > 1 && state < 6) {
            steerGrav(1000, 70000);
        }
        
        if (state == 5 && altitude > 56000) {
            @RAW UNLOCK STEERING.
            @RAW UNLOCK THROTTLE.
            print("Ready for Instructions");
            state = 6;
        }
        
        if (state == 4) {
            if(ETA:APOAPSIS < 5) {
                setThrottle(1);
                state = 5;
            } else {
                if(lfuel < 1) {
                    doStage();
                    doStage();
                    wait(1);
                }
                setThrottle((aposTarget - ETA:APOAPSIS) / aposTarget);
            }
        }
        if (state == 3 && lfuel < 4300) {
            aposTarget = ETA:APOAPSIS + 10;
            setThrottle(0);
            doStage();
            state = 4;
        }
        if (state == 2 && lfuel < 5300) {
            doStage();
            state = 3;
        }
        if (state == 1 && lfuel < 6300) {
            doStage();
            state = 2;
        }
        if (state == 0 && lfuel < 7400) {
            doStage();
            state = 1;
        }
    }

}

main();