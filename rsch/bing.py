import urllib2
import re
import sys
import urlMonkey

#http://api.bing.net/xml.aspx?AppId=Insert your AppId here&Query=msdn%20blogs&Sources=Web&Version=2.0&Market=en-us&Adult=Moderate&Options=EnableHighlighting&Web.Count=10&Web.Offset=0&Web.Options=DisableHostCollapsing+DisableQueryAlterations

BASE = 'http://api.bing.net/xml.aspx?AppId=10342C1A42B35168FA7D07BB62FE118D7E455DD6'
SRC = 'Sources=Web'
V = 'Version=2.0'
MKT = 'Market=en-us'
C = 'Web.Count='

BING = 'http://www.bing.com/search?q=', '&go=&qs=n&sk=&form=QBLH'
YANDEX = 'http://yandex.com/yandsearch?text=', ''

LINKP = re.compile('<web:Url>(.+?)</web:Url>')

def bing_getURL(url):
    return urlMonkey.getURL(url, WIKI=False)
    '''
    c = 0
    #print url
    opener = urllib2.build_opener()
    opener.addheaders = [('User-agent', 'Mozilla/5.0')]
    
    while c < 2:
        try:
            #f = urllib2.urlopen(url)
            f = opener.open(url)
            txt = f.read()
            f.close()
            return txt
        except:
            print sys.exc_info()[0], 'at', url
            c = c+1
            return ''
    '''

def format(str):
    return re.sub(' ', '%20', str)

def query(q, count=10):
    Q = 'Query=' + format(q)
    #print Q
    url = '&'.join([BASE, Q, SRC, V, MKT, C+str(count)])
    
    txt = bing_getURL(url)
    print url
    links = LINKP.findall(txt)
    return links

def directQuery(q, count=10, base=BING):
    url = base[0]+format(q)+base[1]

    txt = bing_getURL(url)
    print url
    links = re.findall('href=\"(.+?)\"', txt)
    if base == BING:
        return links+query(q)
    else:
        return links
    

if __name__ == '__main__':
    print query('site:wikipedia.org Abatement')
