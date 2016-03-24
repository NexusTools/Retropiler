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


function updateScreen() {
    apo = getApoapsis();
    peri = getPeriapsis();
    vel = getSurfaceVelocity();
    fuel = getCurrentStageSolidFuel();
    lfuel = getCurrentStageFuel();
    printXY("APO: " + apo, 2, 1);
    printXY("PERI: " + peri, 2, 2);
    printXY("VEL: " + vel, 2, 3);
    printXY("SFUEL: " + fuel, 2, 4);
    printXY("LFUEL: " + lfuel, 2, 5);
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
            setThrottle(1);
    badclear();
    doStage();
    while (1) {
        updateScreen();
        if (state = 3) {
            if (lfuel < 4300) {
                doStage();
                state = 4;
            }
        }
        if (state = 2) {
            if (lfuel < 5300) {
                doStage();
                state = 3;
            }
        }
        if (state = 1) {
            if (lfuel < 6300) {
                doStage();
                state = 2;
            }
        }
        if (state = 0) {
            if (lfuel < 7400) {
                doStage();
                state = 1;
            }
        }
    }

}

main();