#!/usr/bin/env python
# -*- coding: utf8 -*-


from bs4 import BeautifulSoup
import StringIO
import gzip
import json
import os
import re
import shutil
import urllib2


def download(url):
    response = urllib2.urlopen(url)
    if response.info().get('Content-Encoding') == 'gzip':
        buf = StringIO.StringIO(response.read())
        f = gzip.GzipFile(fileobj=buf)
        data = f.read()
    else:
        data = response.read()
    return data


def crawl_champions():
    shutil.rmtree('../assets/champion', True)
    os.makedirs('../assets/champion/icons')
    data = download('http://lol.duowan.com/hero/')
    soup = BeautifulSoup(data)
    items = []
    for li in soup.select('#champion_list li'):
        key = li.select('a')[0]['href'].strip().split('/')[-2].encode('utf8')
        title = li.select('h3')[0].text.strip().encode('utf8')
        name = li.select('h2')[0].text.strip().encode('utf8')
        icon = li.select('img')[0]['src'].encode('utf8')
        tags = ''
        for tag in li['class']:
            if tag not in ['', 'boy_tag', 'girl_tag']:
                tags += ',' + tag[:-4]
        tags = tags[1:].encode('utf8')
        print key, name, title, tags
        items.append((key, name, title, tags))
        iconData = download(icon)
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
    data = download(url)
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
        iconData = download(imgUrl % (icon))
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
    data = download(url)
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
        iconData = download(spellicon + icon)
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
