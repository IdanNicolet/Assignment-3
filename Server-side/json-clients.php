<?php
$servername = "mysql11.000webhost.com";
$username = "a8510183_root";
$password = "Password1";
$db = "a8510183_clients";

if (!$link = mysql_connect($servername , $username , $password)) {
    echo 'Could not connect to mysql';
    exit;
}

if (!mysql_select_db($db, $link)) {
    echo 'Could not select database';
    exit;
}

	$lan = $_GET["Latitude"];
	$lon = $_GET["Longitude"];
	$pre = $_GET["Pressure"];
	$azi = $_GET["Azimuth"];
	$ber = $_GET["Bearing"];
	$user = $_GET["User"];
	$password = $_GET["Pass"];
	$event = $_GET["Event"];

	$sql = 'SELECT * FROM clients WHERE name=\''.$user.'\'';
	$result = mysql_query ($sql) or die(mysql_error());
	if (mysql_num_rows($result) == 0)
	{
		die("NO SUCH USER");
	}
	else
	{
		$row = mysql_fetch_assoc($result);
		if ($row["password"] != $password)
			die("WRONG PASSWORD");
		else
		{
			$date = date('Y-m-d H:i:s');
			$datetime = explode(" ",$date);
			$time = $datetime[1];
			$sql = 'INSERT INTO cords VALUES (\'' .$lan. '\',\''.$lon.'\','.$pre.'\','.$azi.'\','.$ber.'\','.$event.'\','.$time.');';
			$result = mysql_query($sql, $link);
			echo "{\"positions\":[]}";
		}	
	}

?>