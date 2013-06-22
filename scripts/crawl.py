#!/usr/bin/env python
# -*- coding: utf8 -*-


import json
import os
import shutil
import urllib2


def crawl_champions():
    shutil.rmtree('../assets/champion', True)
    os.makedirs('../assets/champion/icons')
    data = urllib2.urlopen('http://lol.qq.com/app/js/heros.js').read()
    data = data.decode('gbk').encode('utf8')
    data = data[data.find('{'):data.rfind('}') + 1]
    data = json.loads(data)
    items = []
    for id in data:
        key = data[id]['general']['key']
        title = data[id]['general']['title'].encode('utf8')
        name = data[id]['general']['name'].encode('utf8')
        icon = data[id]['general']['icon']
        tags = ','.join(data[id]['general']['tags']).replace(' ', '')
        print key, name, title, tags
        items.append((key, name, title, tags))
        iconData = urllib2.urlopen('http://lol.qq.com/web201007' + icon).read()
        with open('../assets/champion/icons/' + key + '.jpg', 'w') as iconFile:
            iconFile.write(iconData)
    items.sort()
    with open('../assets/champion/champion.txt', 'w') as dbFile:
        for item in items:
            dbFile.write(str.format('{0} {1} {2} {3}\n', *item))


def crawl_items():
    url = 'http://lolbox.duowan.com/js/itemDetailDataForEditor.js'
    imgUrl = 'http://img.lolbox.duowan.com/zb/%d_64x64.png'
    shutil.rmtree('../assets/items', True)
    os.makedirs('../assets/items/icons')
    data = urllib2.urlopen(url).read()
    data = data[data.find('{'):data.rfind('}') + 1]
    data = json.loads(data)
    items = []
    for id in data:
        name = data[id]['name'].encode('utf8')
        attr = data[id]['attr'].encode('utf8')
        filter = data[id]['filter'].replace(' ', ',').encode('utf8')
        price = data[id]['price']
        icon = data[id]['icon']
        print id, name, price, filter, attr
        items.append((id, name, price, filter, attr))
        iconData = urllib2.urlopen(imgUrl % (icon)).read()
        with open('../assets/items/icons/' + str(id) + '.png',
                  'w') as iconFile:
            iconFile.write(iconData)
    items.sort()
    with open('../assets/items/items.txt', 'w') as dbFile:
        for item in items:
            dbFile.write(str.format('{0} {1} {2} {3} {4}\n', *item))


def main():
    crawl_champions()
    crawl_items()


if __name__ == '__main__':
    main()
