from typing import List, Optional

from dotenv import find_dotenv
from pydantic_settings import BaseSettings
from pydantic import SecretStr
from pydantic.fields import Field


class Settings(BaseSettings):
    database_url: str
    bot_token: SecretStr
    bot_chat_id: str
    limit_q: int
    freq_sec: int
    utc_shift: int
    night_time: Optional[str] = Field(None, pattern="^[0-9]+:[0-9]+$")

    class Config:
        env_file: str = find_dotenv(".env")
        env_file_encoding = "utf-8"


settings = Settings()
