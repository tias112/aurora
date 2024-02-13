#!/bin/bash
sudo apt update
yes | sudo  apt-get install python3-venv
yes | sudo apt-get install python-dev-is-python3
yes | sudo apt-get install libpq-dev
yes | sudo  apt-get install python3-pip
sudo python -m pip install -U pip wheel setuptools
cd /home/ubuntu
sudo git clone https://github.com/tias112/aurora.git
cd aurora
pip install -r requirements.txt

echo "export DATABASE_URL=postgres://${username}:${password}@${host}:${port}/${db_name}" > run.sh
echo "cd /home/ubuntu/aurora" >> run.sh
echo "python start_observer.py 0 60 ${utc_shift} ${bot_token} -773104628 ${night_time}" >> run.sh
chmod a+x run.sh


sudo cat <<EOF > /etc/systemd/system/aurora.service
[Unit]
Description=Aurora systemd service.

[Service]
Type=simple
ExecStart=/bin/bash /home/ubuntu/aurora/run.sh

[Install]
WantedBy=multi-user.target

EOF
sudo chmod 644 /etc/systemd/system/aurora.service
sudo systemctl start aurora
