<?php

error_reporting(E_ALL);
set_time_limit(0);

$address = '0.0.0.0';
$port    = 7452;
$mysqli  = new mysqli('localhost:3306', 'root', '', 'grafologus');

if ($mysqli->connect_error) {
    die('Connect Error (' . $mysqli->connect_errno . ') ' . $mysqli->connect_error);
}
echo "Adatbazis csatlakozas sikeres!\n";

if (($sock = socket_create(AF_INET, SOCK_STREAM, SOL_TCP)) === false) {
    echo "socket_create() falló: razón: " . socket_strerror(socket_last_error()) . "\n";
}

if (socket_bind($sock, $address, $port) === false) {
    echo "socket_bind() falló: razón: " . socket_strerror(socket_last_error($sock)) . "\n";
}

if (socket_listen($sock, 5) === false) {
    echo "socket_listen() falló: razón: " . socket_strerror(socket_last_error($sock)) . "\n";
}

$clients             = array(); //Kliensek tömbje
$grafologusconnected = 0;
$paciensconnected    = 0;
$grafszam            = -8;
$pacszam             = -8;
$connection          = false;
$gkey                = -8;
$pkey                = -8;
$id                  = -8;
$finish              = false;
$today               = date("y.m.d");
$partner             = -8;
do {
    $read   = array();
    $read[] = $sock;
    
    $read = array_merge($read, $clients);
    
    if (socket_select($read, $write, $except, $tv_sec = 5) < 1) {
        continue;
    }
    
    // Új kapcsolatok kezelése
    if (in_array($sock, $read)) {
        
        if (($msgsock = socket_accept($sock)) === false) {
            echo "socket_accept() hiba: " . socket_strerror(socket_last_error($sock)) . "\n";
            break;
        }
        $clients[] = $msgsock;
        $key       = array_keys($clients, $msgsock);
    }
    
    // Beérkezõ üzenetek kezelése
    foreach ($clients as $key => $client) {
        if (in_array($client, $read)) {
            
            //Mindig megkeresi a hozzátartozó partnert (ha kapcsolódva van)
            if ($connection) {
                if ($clients[$key] == $gkey) {
                    $partner = $pkey;
                } else if ($clients[$key] == $pkey) {
                    $partner = $gkey;
                }
            }
            //Beolvas
            if (false === ($buf = socket_read($client, 2048, PHP_NORMAL_READ))) {
                echo "Kilep\n";
                ClientQuit($client, $clients[$key], $connection, $partner, $clients);
                break 2;
            }
            //Szóközök átugrása
            if (!$buf = trim($buf)) {
                continue;
            }
            //Ha már létrehoztak kapcsolatot, vagy már van ugyanolyan fél belépve
            if ((($buf == 'grafologus' || $buf == 'paciens') && $connection) || ($buf == 'grafologus' && $gkey != -8) || ($buf == 'paciens' && $pkey != -8)) {
                $cantconnect = "No more partner\n";
                socket_write($client, $cantconnect, strlen($cantconnect));
                unset($clients[$key]);
                socket_close($client);
            }
            //Bejelentkezik egy grafológus
            else if ($buf == 'grafologus') {
                if (false === ($graphologistname = socket_read($client, 2048, PHP_NORMAL_READ))) {
                    echo "socket_read() falló: razón: " . socket_strerror(socket_last_error($client)) . "\n";
                    break 2;
                }
                $grafologusconnected = 1;
                $grafszam            = $key;
                $gkey                = $clients[$key];
                echo "Bejelentkezett egy grafologus: {$gkey}\n";
                
                //Bejelentkezik egy páciens
            } else if ($buf == 'paciens') {
                if (false === ($patientname = socket_read($client, 2048, PHP_NORMAL_READ))) {
                    echo "socket_read() falló: razón: " . socket_strerror(socket_last_error($client)) . "\n";
                    break 2;
                }
                $paciensconnected = 1;
                $pacszam          = $key;
                $pkey             = $clients[$key];
                echo "Bejelentkezett egy paciens: {$pkey}\n";
                
            }
            //Kilépés esete
            if ($buf == 'quit' && ($client == $gkey || $client == $pkey)) {
                ClientQuit($client, $clients[$key], $connection, $partner, $clients);
                $finish = true;
                break;
            }
            //Chat üzenet érkezik
            else if ($buf == 'chat') {
                echo "Chat uzenetet kuld az egyik fel.\n";
                $text = "";
                echo "Chat üzenetet fogok kapni\n";
                if (false === ($text = socket_read($client, 1024))) {
                    echo "socket_read() hiba: " . socket_strerror(socket_last_error($client)) . "\n";
                    break 2;
                } else {
                    echo " Megkaptam az üzenetet : {$text}\n";
                    $chat = "chat\n";
                    socket_write($partner, $chat, strlen($chat));
                    socket_write($partner, $text, strlen($text));
                    
                }
                echo "Tovabbitottam a chat uzenetet\n";
                
                //Belépéskor mindkét fél elküldi a kódját (grafológus esetében jelszó, páciensnél azonosító)
            } else if ($buf == "patientid") {
                if (false === ($id = socket_read($client, 2048, PHP_BINARY_READ))) {
                    echo "socket_read() falló: razón2: " . socket_strerror(socket_last_error($client)) . "\n";
                    break 2;
                }
                if ($client == $gkey) {
                    $name   = trim($graphologistname);
                    $result = mysqli_query($mysqli, "SELECT Password FROM graphologist_users where Name='$name';");
                    if (!$result) {
                        printf("Error: %s\n", mysqli_error($mysqli));
                        exit();
                    }
                    $row  = mysqli_fetch_array($result);
                    $id   = substr($id, 0, -1);
                    $text = "";
                    if (sha1($id) === $row[0]) {
                        $text = "Password OK.\n";
                        $gid  = $id;
                    } else {
                        $text = "Wrong Password.\n";
                        echo sha1($id);
                    }
                    socket_write($client, $text, strlen($text));
                } else {
                    $pid = $id;
                }
                //Grafológus befejezte a vizsgálatot
            } else if ($buf == 'finishexam') {
                socket_write($partner, $buf, strlen($buf));
                unset($clients[$key]);
                socket_close($client);
                unset($clients[$partner]);
                socket_close($partner);
                $finish = true;
                //Páciens küldi a rajz koordinátáit
            } else if ($buf == 'koord' && $client == $pkey) {
                echo "Kapom a koordinatakat a pacienstol...\n";
                if (false === ($drawingname = socket_read($client, 2048, PHP_BINARY_READ))) {
                    echo "socket_read() falló: razón2: " . socket_strerror(socket_last_error($client)) . "\n";
                    break 2;
                }
               $drawingname = substr($drawingname, 0, -1);
                $allkoords   = "";
                $koords      = "";
                while (strpos($koords, 'end') != true) {
                    if (false === ($koords = socket_read($client, 2048, PHP_BINARY_READ))) {
                        echo "socket_read() falló: razón2: " . socket_strerror(socket_last_error($client)) . "\n";
                        break 2;
                    } else {
                        $allkoords .= $koords;
                    }
                    
                }
                echo "koordinatak: {$allkoords}\n";
                $text = "grafologus kap\n";
                socket_write($partner, $text, strlen($text));
                socket_write($partner, $allkoords, strlen($allkoords));
                echo "Megkaptam. Tovabbitottam.Most lementem az adatbazisba.\n";
                $sql = "INSERT INTO drawings (DrawingData, FullName,DrawingName,PatientId,DrawingDate) VALUES ('$allkoords', '$patientname','$drawingname','$pid','$today')";
                if (!mysqli_query($mysqli, $sql)) {
                    die('Error: ' . mysqli_error($mysqli));
                }
                echo "Elkuldve + lementve\n";
                //Lekérdezzük hogy melyik id-ba mentette, és ezt elküldjük
                $id = -8;
                $id = $mysqli->insert_id . "\n";
                socket_write($partner, $id, strlen($id));
                //Grafológus kitöröl egy rajzot
            } else if ($buf == 'Delete' && $client == $gkey) {
                echo "Torolni fogok\n";
                if (false === ($id = socket_read($client, 2048, PHP_NORMAL_READ))) {
                    echo "socket_read() falló: razón: " . socket_strerror(socket_last_error($client)) . "\n";
                    break 2;
                }
                $id = substr($id, 0, -1);
                echo "kaptam: {$id}\n";
                mysqli_query($mysqli, "DELETE FROM drawings WHERE id='$id'");
                echo "Toroltem a {$id}. sort\n";
            } else if ($buf == 'GetDrawingsList') {
                echo "Kerdesek neveit kertek...\n";
                $text = "GetQuestions\n";
                socket_write($client, $text, strlen($text));
                echo " neve : {$patientname} id-ja : {$pid}\n";
                $result = mysqli_query($mysqli, "SELECT id,DrawingName,DrawingDate FROM drawings where FullName='$patientname' and PatientId='$pid';");
                if (!$result) {
                    printf("Error: %s\n", mysqli_error($mysqli));
                    exit();
                }
                $num = mysqli_num_rows($result);
                socket_write($client, $num . "\n", strlen($num . "\n"));
                while ($row = mysqli_fetch_array($result)) {
                    $drawingid       = trim($row[0]) . "\n";
                    $drawingnamedate = trim($row[1]) . " - " . trim($row[2]) . "\n";
                    socket_write($client, $drawingid, strlen($drawingid));
                    socket_write($client, $drawingnamedate, strlen($drawingnamedate));
                    echo "neve : {$drawingnamedate}\n";
                }
                echo "Elkuldve.\n";
                
                //Korábbi rajz adatait lekéri a grafológus
            } else if ($buf == 'GetDrawing') {
                if (false === ($drawingid = socket_read($client, 2048, PHP_NORMAL_READ))) {
                    echo "socket_read() falló: razón: " . socket_strerror(socket_last_error($client)) . "\n";
                    break 2;
                }
                $text = "Get selected drawing data\n";
                socket_write($client, $text, strlen($text));
                $result = mysqli_query($mysqli, "SELECT DrawingData FROM drawings where id='$drawingid';");
                if (!$result) {
                    printf("Error: %s\n", mysqli_error($mysqli));
                    exit();
                }
                while ($row = mysqli_fetch_array($result)) {
                    socket_write($client, $row[0] . "\n", strlen($row[0] . "\n"));
                }
                echo "elkuldtem.\n";
                //Grafológus utasítást küld a páciensnek
            } else if ($connection && $client == $gkey) {
                $buf = "{$buf}\n";
                socket_write($partner, $buf, strlen($buf));
            }
            //Létrejött a kapcsolat
            if ($grafologusconnected && $paciensconnected && $grafszam != $pacszam) {
                echo "Kapcsolat!\n";
                $kapcsolat = "Kapcsolat\n";
                foreach ($clients as $key => $client) {
                    socket_write($client, $kapcsolat, strlen($kapcsolat));
                }
                $grafologusconnected = 0;
                $paciensconnected    = 0;
                $connection          = true;
            }
            
        }
        
    }
} while (!$finish);

socket_close($sock);
function ClientQuit($client, $unsetclient, $connection, $partner, $clients)
{
    echo "Kilepett a {$client}";
    unset($unsetclient);
    socket_close($client);
    if ($connection) {
        $kapcsolat = "Megszakadt a kapcsolat!\n";
        socket_write($partner, $kapcsolat, strlen($kapcsolat));
        sleep(2);
        unset($clients[$partner]);
        socket_close($partner);
    }
    
}

?>