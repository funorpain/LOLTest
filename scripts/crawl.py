#!/usr/bin/env python
# -*- coding: utf8 -*-


import json
import os
import re
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


def crawl_spells():
    url = 'http://lol.duowan.com/s/js/spells.js'
    spellicon = 'http://lol.duowan.com/s/images/'
    shutil.rmtree('../assets/spells', True)
    os.makedirs('../assets/spells/icons')
    data = urllib2.urlopen(url).read()
    data = re.search(r'var spells=(\[.*?\]);', data, re.M | re.S).group(1)
    data = data.replace('spellicon+"', '"')
    data = re.sub(r'([{,])([a-z]+):', '\\1"\\2":', data)
    data = json.loads(data)
    items = []
    for item in data:
        name = item['name'].encode('utf8')
        icon = item['icon'].encode('utf8')
        m = re.match(r'^tf_(\d+)\.jpg$', icon)
        if not m:
            raise Exception('unexpected icon value: ' + icon)
        id = int(m.group(1))
        level = item['level'].encode('utf8')
        description = item['description'].encode('utf8')
        print "%d\t%s\t%s\t%s" % (id, name, level, description)
        items.append((id, name, level, description))
        iconData = urllib2.urlopen(spellicon + icon).read()
        with open('../assets/spells/icons/' + str(id) + '.jpg',
                  'w') as iconFile:
            iconFile.write(iconData)
    items.sort()
    with open('../assets/spells/spells.txt', 'w') as dbFile:
        for item in items:
            dbFile.write(str.format('{0}\t{1}\t{2}\t{3}\n', *item))


def main():
    crawl_champions()
    crawl_items()
    crawl_spells()


if __name__ == '__main__':
    main()
