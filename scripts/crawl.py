#!/usr/bin/env python
# -*- coding: utf8 -*-


import json
import os
import shutil
import urllib2


def main():
    shutil.rmtree('../assets/champion', True)
    os.makedirs('../assets/champion/icons')
    data = urllib2.urlopen('http://lol.qq.com/app/js/heros.js').read()
    data = data.decode('gbk').encode('utf8')
    data = data[data.find('{'):data.rfind('}') + 1]
    data = json.loads(data)
    items = []
    for id in data:
        key = data[id]['general']['key']
        name = data[id]['general']['name'].encode('utf8')
        icon = data[id]['general']['icon']
        tags = ','.join(data[id]['general']['tags']).replace(' ', '')
        print key, name, tags
        items.append((key, name, tags))
        iconData = urllib2.urlopen('http://lol.qq.com/web201007' + icon).read()
        with open('../assets/champion/icons/' + key + '.jpg', 'w') as iconFile:
            iconFile.write(iconData)
    items.sort()
    with open('../assets/champion/champion.txt', 'w') as dbFile:
        for item in items:
            dbFile.write(str.format('{0} {1} {2}\n', *item))


if __name__ == '__main__':
    main()
