<?php

//date_default_timezone_set('Asia/Jerusalem');
$servername = "mysql11.000webhost.com";
$username = "a8510183_root";
$password = "Password1";
$db = "a8510183_clients";
$DEBUG = TRUE;

if (!$link = mysql_connect($servername , $username , $password)) {
    echo 'Could not connect to mysql';
    exit;
}

if (!mysql_select_db($db, $link)) {
    echo 'Could not select database';
    exit;
}
// http://matchrace.net16.net/insertClient.php?table=clients&Latitude=11&Longitude=10&Pressure=0&Azimuth=0&Bearing=0&Information=user1_bbb_1&Event=1
$lat = make_safe($_GET["Latitude"]);
$lon = make_safe($_GET["Longitude"]);
$speed = make_safe($_GET["Pressure"]);
$azi = make_safe($_GET["Azimuth"]);
$ber = make_safe($_GET["Bearing"]);
$info = explode("_", $_GET["Information"]);
$user = make_safe($info[0]);
$password = make_safe($info[1]);
$event = make_safe($_GET["Event"]);
$time = make_safe($_GET["Time"]);

if (startsWith($user, "newSailor"))
// register new user
{
	$user = str_replace ("newSailor", "", $user);
	$sql = 'SELECT * FROM clients WHERE name=\''.$user.'\'';
	$result = mysql_query ($sql) or die(mysql_error());
	if (mysql_num_rows($result) == 0)
	{
		$sql = 'INSERT INTO clients VALUES (\'' .$user. '\',\''.$password.'\','.$event.');';
		$result = mysql_query($sql, $link);
		if ($result)
			echo "OK";
		else
			echo "NOT OK<br>ERROR";
	}
	else
	{
		echo "NOT OK<br>User Already registered";
	}
	if ($DEBUG) echo $sql;
} else if(startsWith($user, "BuoyNum"))
// enter new buoy
{	
	$user = str_replace ("BuoyNum", "", $user);
	//echo $user;
	$sql = 'SELECT * FROM events WHERE event=\''.$event.'\'';
	$result = mysql_query ($sql) or die(mysql_error());
	if (mysql_num_rows($result) == 0)
	// open new event
	{
		$sql = 'INSERT INTO events (event, lat'.$user.', lon'.$user.') VALUES (\'' .$event. '\',\''.$lat. '\',\''.$lon.'\');';
		$result = mysql_query ($sql) or die(mysql_error());
		if ($result)
			echo "OK";
		else
			echo "NOT OK<br>ERROR inserting buoy<br>";
		if ($DEBUG) echo $sql;
	} else {
	// add new buoy in existing event
		$sql = 'UPDATE events SET lat'.$user.'='.$lat.'  WHERE event ='.$event;
		if ($DEBUG) echo $sql;
		$result = mysql_query ($sql) or die(mysql_error());
		if (!$result)
			die("NOT OK<br>ERROR inserting buoy<br>");
			
		$sql = 'UPDATE events SET lon'.$user.'='.$lon.'  WHERE event ='.$event;
		if ($DEBUG) echo $sql;		
		$result = mysql_query ($sql) or die(mysql_error());
		if ($result)
			echo "OK";
		else
			die("NOT OK<br>ERROR inserting buoy<br>");
	}
} else if(startsWith($user, "Cord")) {
// insert new cordinate
	$user = str_replace ("Cord", "", $user);
	$user = str_replace ("Sailor", "", $user);

	$sql = 'SELECT * FROM clients WHERE name=\''.$user.'\'';
	$result = mysql_query ($sql) or die(mysql_error());
	$row = mysql_fetch_assoc($result);
	if ($row["password"] != $password)
		die("NOT OK<br>Failed authentication");		// failed authentication
		
	$sql = 'INSERT INTO cords VALUES (\'' .$lat. '\',\''.$lon. '\',\''.$speed. '\',\''.$azi. '\',\''.$ber. '\',\''.$user. '\',\''.$event.'\',\'' . $time . '\');';
	if ($DEBUG) echo $sql;
	$result = mysql_query ($sql) or die(mysql_error());
	if ($result)
		echo "OK";
	else
		echo "NOT OK<br>ERROR inserting cords<br>";
}

function startsWith($haystack, $needle)
{
     $length = strlen($needle);
     return (substr($haystack, 0, $length) === $needle);
}

function endsWith($haystack, $needle)
{
    $length = strlen($needle);
    if ($length == 0) {
        return true;
    }

    return (substr($haystack, -$length) === $needle);
}

function make_safe($variable) 
{
   $variable = strip_tags(mysql_real_escape_string(trim($variable)));
   return $variable; 
}

?>