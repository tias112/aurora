import base64

import json
import re
import urllib
from common.noaa_client import NOAAClient

print('Loading function')


def lambda_handler(event, context):
    output = []
    noaa_client = NOAAClient()
    res =  noaa_client.process()
    print(res)
    bz_window = [t['bz'] for t in res]
    try:
        for record in event['records']:
            payload = base64.b64decode(record['data']).decode('utf-8')
            json_object = json.loads(payload)
            # Do custom processing on the payload here
            json_object['data_array'] = bz_window
            output_record = {
                'recordId': record['recordId'],
                'result': 'Ok',
                'data': base64.b64encode(json.dumps(json_object).encode('utf-8')).decode('utf-8')
            }
            output.append(output_record)
    except Exception as e:
        print(e)
        
    print('Successfully processed {} records.'.format(len(event['records'])))

    return {'records': output}
