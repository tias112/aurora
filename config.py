import pathlib
from typing import List, Optional

from dotenv import find_dotenv, load_dotenv
from pydantic_settings import BaseSettings
from pydantic.fields import Field
class Settings(BaseSettings):
    database_url: str


    class Config:
        env_file: str = find_dotenv(".env")
        env_file_encoding = "utf-8"

load_dotenv()
settings = Settings()