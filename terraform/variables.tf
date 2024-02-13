variable "env_time_shift_from_utc" {
    default = "2"
}

variable "public_ip" {
    default = "true"
}

variable "night_time_start" {
    default = "17"
}
variable "night_time_end" {
    default = "5"
}

variable "bot_token" {
  description = "It's a secret"
  type        = string
  sensitive   = true
}
