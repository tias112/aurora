## AWS

1. install terraform
2. create terraform.tfvars with telegram bot_token
3. create aws account (store credentials)
4. create default vpc. subnet should have enabled public assigned address
5. ```terraform init ```
6. ```terraform apply```

## Notes
about automated start/stop of observer:

create s3 resource and upload terraform files.

create policy for s3 get:
{
  "Id": "Policy1708898708697",
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Stmt1708898703214",
      "Action": [
        "s3:GetObject"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::aurora-1/*",
      "Principal": "*"
    }
  ]
}

###start