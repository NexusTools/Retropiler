
function setupBodyProperties(){
@RAW set b to body.
@RAW set mu to 0.
@RAW if b = "Kerbin" {
@RAW     set mu to 3.5316000*10^12.  // gravitational parameter, mu = G mass
@RAW     set rb to 600000.           // radius of body
@RAW     set ha to 69077.            // atmospheric height
@RAW     set lorb to 80000.          // low orbit altitude
@RAW }
@RAW if b = "Mun" {
@RAW     set mu to 6.5138398*10^10.
@RAW     set rb to 200000.
@RAW     set ha to 0.
@RAW     set lorb to 14000. 
@RAW }
@RAW if b = "Minmus" {
@RAW     set mu to 1.7658000*10^9.
@RAW     set rb to 60000.
@RAW     set ha to 0.
@RAW     set lorb to 10000. 
@RAW }

}

function setAponodeTarget(alt){
    setupBodyProperties();
    // present orbit properties
@RAW set vom to velocity:orbit:mag.  // actual velocity
@RAW set r to rb + altitude.         // actual distance to body
@RAW set ra to rb + apoapsis.        // radius in apoapsis
@RAW set va to sqrt( vom^2 + 2*mu*(1/ra - 1/r) ). // velocity in apoapsis
@RAW set a to (periapsis + 2*rb + apoapsis)/2. // semi major axis present orbit
@RAW // future orbit properties
@RAW set r2 to rb + apoapsis.    // distance after burn at apoapsis
@RAW set a2 to (alt + 2*rb + apoapsis)/2. // semi major axis target orbit
@RAW set v2 to sqrt( vom^2 + (mu * (2/r2 - 2/r + 1/a - 1/a2 ) ) ).
@RAW // setup node 
@RAW set deltav to v2 - va.
@RAW set x to node(time:seconds + eta:apoapsis, 0, 0, deltav).
@RAW add x.
@RAW // workaround for scientic numbers bug on load
@RAW set mu to 0.

}

function steerGrav(gt0, gt1){
@RAW    set ar to alt:radar.
@RAW    if ar > gt0 and ar < gt1 {
@RAW        set arr to (ar - gt0) / (gt1 - gt0).
@RAW        set pda to (cos(arr * 180) + 1) / 2.
@RAW        set theta to 90 * ( pda - 1 ).
@RAW        lock steering to up + R(0, theta, 0).
@RAW    }
@RAW    if ar > gt1 {
@RAW        lock steering to up + R(0, theta, 0).
@RAW    }
}

function setPerinodeTarget(alt){

// constants: mu, rb
setupBodyProperties();
// present orbit properties
@RAW set vom to velocity:orbit:mag.  // actual velocity
@RAW set r to rb + altitude.         // actual distance to body
@RAW set ra to rb + periapsis.        // radius in periapsis
@RAW set va to sqrt( vom^2 + 2*mu*(1/ra - 1/r) ). // velocity in periapsis
@RAW set a to (periapsis + 2*rb + apoapsis)/2. // semi major axis present orbit
@RAW // future orbit properties
@RAW set r2 to rb + periapsis.    // distance after burn at periapsis
@RAW set a2 to (alt + 2*rb + periapsis)/2. // semi major axis target orbit
@RAW set v2 to sqrt( vom^2 + (mu * (2/r2 - 2/r + 1/a - 1/a2 ) ) ).
@RAW // setup node 
@RAW set deltav to v2 - va.
@RAW set x to node(time:seconds + eta:periapsis, 0, 0, deltav).
@RAW add x.
@RAW // workaround for scientific numbers bug on load
@RAW set mu to 0.

}