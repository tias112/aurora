import requests
import logging
from telegram.ext import ConversationHandler, Updater, CommandHandler, MessageHandler, Filters
import os
import re
from .db_client import *

db = DBClient()
ENTER_Q = 1

def telegram_bot_sendtext(bot_token, bot_chatID, bot_message):
   send_text = 'https://api.telegram.org/bot' + bot_token + '/sendMessage?chat_id=' + bot_chatID + '&parse_mode=Markdown&text=' + bot_message
   response = requests.get(send_text)
   return response.json()

# Enable logging
logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.INFO)

logger = logging.getLogger(__name__)
TOKEN = 'YOURTELEGRAMBOTTOKEN'

# Define a few command handlers. These usually take the two arguments update and
# context. Error handlers also receive the raised TelegramError object in error.
def start(update, context):
    """Send a message when the command /start is issued."""
    print("subscribe notification", update.message.chat.id)
    credentials = db.execute_fetch_all("SELECT telegram FROM Users WHERE telegram ='%s'", (update.message.chat.id,))
    if len(credentials) == 0:
        db.execute_query("INSERT INTO users(telegram,min_q,telegramnotification) VALUES(%s,3,true)", (update.message.chat.id,))
        update.message.reply_text('Hi! choose minimum q (1-9) for notify:')
        return ENTER_Q
    else:
        update.message.reply_text("Notifications are already registered.\nUse /stop")
        return ConversationHandler.END

def get_min_q(update, context):
    pattern = re.compile("^[0-9]$")
    if pattern.match(update.message.text):
        min_q = int(update.message.text)
        db.execute_query("UPDATE users SET min_q=%s where telegram='%s'", (min_q, update.message.chat.id,))
        update.message.reply_text(f"started observing for q>={update.message.text}\n")
    else:
        update.message.reply_text("incorrect input")
        return ConversationHandler.END
    # next state in conversation

def stop(update, context):
    """Send a message when the command /start is issued."""
    telegr = db.execute_fetch_all("SELECT telegram FROM Users WHERE telegram = '%s'", (update.message.chat.id,))[0]
    if telegr is not None:
        db.execute_query("DELETE FROM users WHERE telegram = %s", (telegr[0],))
        update.message.reply_text("Notifications are disabled.")
        print("stopping notify for", telegr[0])
    else:
        update.message.reply_text("You must start first.\nUse /start")
        return ConversationHandler.END

def help(update, context):
    """Send a message when the command /help is issued."""
    update.message.reply_text('Use /start to subscribe \n Use /stop to turn off notifications ')

def echo(update, context):
    """Echo the user message."""
    update.message.reply_text(update.message.text)

def error(update, context):
    """Log Errors caused by Updates."""
    logger.warning('Update "%s" caused error "%s"', update, context.error)

def cancel(update, context):
    update.message.reply_text('canceled')
    # end of conversation
    return ConversationHandler.END

def test_user(chat_id):
    credentials = db.execute_fetch_all("SELECT telegram FROM users WHERE telegram ='%s'", (chat_id,))
    if len(credentials) == 0:
        #msg = tb.send_message(m.chat.id, "Tell me your e-mail: ") #TODO enter minimum q
        #tb.register_next_step_handler(msg, reg)
        db.execute_query("INSERT INTO users VALUES(%s,3,true)", (chat_id,))
        db.execute_query("UPDATE users SET min_q=%s where telegram='%s'", (5, chat_id,))
        #update.message.reply_text('Hi!')
    else:
        print("User already registered.\nUse /registered")
    print("test user created")


def bot_main(token):
    """Start the bot."""
    print("Start the bot")
    #test_user("-773104628")
    PORT = int(os.environ.get('PORT', 5000))
    TOKEN = token
    # Create the Updater and pass it your bot's token.
    # Make sure to set use_context=True to use the new context based callbacks
    # Post version 12 this will no longer be necessary
    updater = Updater(TOKEN, use_context=True)

    # Get the dispatcher to register handlers
    dp = updater.dispatcher

    conversation_handler = ConversationHandler(
       entry_points=[CommandHandler('start', start)],
       states={
           ENTER_Q: [
               CommandHandler('cancel', cancel),  # has to be before MessageHandler to catch `/cancel` as command, not as `title`
               MessageHandler(Filters.text, get_min_q)
           ]
       },
       fallbacks=[CommandHandler('cancel', cancel)]
    )
    # on different commands - answer in Telegram
    dp.add_handler(conversation_handler)
    dp.add_handler(CommandHandler("stop", stop))
    dp.add_handler(CommandHandler("help", help))

    # on noncommand i.e message - echo the message on Telegram
    dp.add_handler(MessageHandler(Filters.text, echo))

    # log all errors
    dp.add_error_handler(error)

    # Start the Bot
    #updater.start_webhook(listen="0.0.0.0",
    #                      port=int(PORT),
    #                      url_path=TOKEN)
    #updater.bot.setWebhook('http://185.255.132.58/' + TOKEN)
    updater.start_polling()
    # Run the bot until you press Ctrl-C or the process receives SIGINT,
    # SIGTERM or SIGABRT. This should be used most of the time, since
    # start_polling() is non-blocking and will stop the bot gracefully.
    updater.idle()
    db.close_connection()
    print("idle")
