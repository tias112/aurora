locals {

  postgres_database_name = "mydb"
  postgres_username     = "aurora"
  postgres_instance_name = "mydb"
  postgres_port          = "5432"
  night_time             = join(":", [var.night_time_start, var.night_time_end])
}

terraform {
  required_providers {
    postgresql = {
      source = "cyrilgdn/postgresql"
      version = "~> 1.13.0"
    }
  }
}
// PROVIDERS

provider "aws" {
  region                  = "eu-north-1"
  shared_credentials_files  = ["credentials"]
  profile                 = "default"
}

//KEYS

resource "tls_private_key" "example" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "generated_key" {
  key_name   = "my-key"
  public_key = tls_private_key.example.public_key_openssh
}

resource "random_string" "rds_password" {
   length = 12
   special = true
   override_special = "!#"
}

resource "aws_ssm_parameter" "rds_password" {
    name = "/aurora/psql"
    description = "master pwd"
    type= "SecureString"
    value = random_string.rds_password.result
}

data "aws_ssm_parameter" "my_rds_password" {
  name   = "/aurora/psql"
  depends_on = [aws_ssm_parameter.rds_password]
}



provider "postgresql" {
  host            = aws_db_instance.postgres.address
  port            = local.postgres_port
  database        = local.postgres_database_name
  username        = local.postgres_username
  password        = data.aws_ssm_parameter.my_rds_password.value
  sslmode         = "require"
  connect_timeout = 15
  superuser       = false
}

// POSTGRES
resource "aws_security_group" "security_group_aurora" {
  name = "security_group_aurora"
  description = "Allow all inbound traffic"
  #vpc_id      = aws_vpc.main.id
  ingress {
    from_port   = local.postgres_port
    to_port     = local.postgres_port
    protocol    = "tcp"
    description = "PostgreSQL"
    cidr_blocks = ["0.0.0.0/0"] // >
  }
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    description = "PostgreSQL"
    cidr_blocks = ["0.0.0.0/0"] // >
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol = -1
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "postgres" {
  allocated_storage      = 8
  storage_type           = "gp2"
  engine                 = "postgres"
  engine_version         = "14"
  instance_class         = "db.t3.micro"
  db_name                = local.postgres_instance_name
  username               = local.postgres_username
  password               = data.aws_ssm_parameter.my_rds_password.value
  publicly_accessible    = false
  #parameter_group_name   = "default.postgres12"
  vpc_security_group_ids = [aws_security_group.security_group_aurora.id]
  skip_final_snapshot    = true
}



resource "aws_instance" "my_aurora" {
   ami = "ami-0014ce3e52359afbd"
   key_name      = aws_key_pair.generated_key.key_name
   instance_type = "t3.micro"
   associate_public_ip_address = var.public_ip
   vpc_security_group_ids = [aws_security_group.security_group_aurora.id]
   user_data = templatefile("start_ec2.sh.tpl", {
      db_name                = local.postgres_instance_name
      username               = local.postgres_username
      password               = data.aws_ssm_parameter.my_rds_password.value
      port                   = local.postgres_port
      host                   = aws_db_instance.postgres.address
      utc_shift              = var.env_time_shift_from_utc
      night_time             = local.night_time
      bot_token              = var.bot_token
   })
}

output "private_key" {
  value     = tls_private_key.example.private_key_pem
  sensitive = true
}

output "rds_password" {
  value     = data.aws_ssm_parameter.my_rds_password.value
  sensitive = true
}

output "public_ip" {
  value     = var.public_ip
}