import re
import urllib2
import sys
import time
import cgi
import shelve
import urlMonkey

BASEURL = 'http://en.wikipedia.org/w/api.php'
pfound = re.compile('totalhits=\"0\"')

'''
Func spec:
  - acq taxonomy
  - acq text
  - 
'''
def get_text(article, dir):
	file = open(dir+article['file'])
	text = ""
	
	file.seek(article['start'])
	
	#for i in range(article['start'], article['end']):
	#	text += file.readline()
	text = file.read(article['end'] - article['start'])
	
	file.close()
	
	return text


class ShelveMonkey:
    def __init__(self, dir='', name='shelves/sasho2.shlv'):
        self.shelve = shelve.open(name)
        self.dir = dir
        self.miss = 0
        self.hit = 0
        print 'shelve monkey loaded'

    def getArticleText(self, title):
        title = re.sub('_', ' ', title)
        try:
                if title in self.shelve:
                        self.hit+=1
                        d = self.shelve[title]
                        if type(d) == type({}):
                                return d['text']

                self.miss+=1
        except:
                pass



#SHM = ShelveMonkey()


def reformat(title):
    title = re.sub('[\r\n\t]', '', title.strip())
    return '_'.join(title.split(' '))
    if False:
        title = re.sub('[\r\n\t]', '', title)
        parts = re.split('[ _]', title.lower())
        res = []
        
        for p in parts:
            if len(p) == 0:
                continue
            s = p[0].upper()+p[1:]
            res.append(s)
        return '_'.join(res)

def search(title, base=BASEURL):
    title = re.sub(' ', '_', title)
    url = base+'?action=query&format=xml&list=search&srsearch='+title
    txt = urlMonkey.getURL(url)
    
    #print txt
    return re.findall('suggestion=\"(.+?)\"', txt) + re.findall('title=\"(.+?)\"', txt)

def getCategories(title, base=BASEURL):
    title = reformat(title)
    url = base+'?action=query&format=xml&titles='+title+'&prop=categories&cllimit=45'
    txt = urlMonkey.getURL(url)
    return filter(lambda x:
                  'Redirects' not in x
                  and 'All' not in x
                  and 'Articles' not in x,
                  re.findall('title=\"Category:(.+?)\"', txt))

def testGetMetaData():
    qs=['William%20Shakespeare','Capacitor', 'Monkey', 'Bulgaria', 'Ben%20Bernanke', 'God']
    for q in qs:
        print getMetadata(q)
        print '============================'


def getText(title, base=BASEURL, rfmt=None, shelve_only=False):
    #returns text with specified wiki titles
    title = re.sub(' ', '_', title)

    #check local copy first
    try:
            txt = SHM.getArticleText(title)
            if txt != None:
                    return txt
    except:
            pass

    if shelve_only:
        print 'miss', title
        return ''
    
    if rfmt != None:
        title = rfmt(title)
    url = base+'?action=query&titles='+title+'&format=xml&prop=revisions&rvprop=content&redirects'
    #print url in urlMonkey.UCH.cache_dict
    
    txt = urlMonkey.getURL(url)
    return txt

def getArticleText(title):
        txt = getText(title)
        txt = ' '.join(txt.split('\'\'\'')[1:])
        txt = re.sub('^<.+?\'\'\'(?s)|<.+?>|{{.+?}}(?s)', '', txt)
        return txt

def getLinkToArticle(title, base=BASEURL):
    title = reformat(title)
    try:
        title = getSearchSuggestions(title)[0]
        url = base+'?action=query&titles='+title+'&format=xml&prop=info&inprop=url&redirects'
        txt = urlMonkey.getURL(url)
        return re.search('fullurl=\"(.+?)\"', txt).group(1)
    except:
        return '#NA'

def getSearchSuggestions(title, base=BASEURL, limit=20):
    title=reformat(title)
    url = base+'?action=opensearch&search='+title+'&format=xml&llimit='+str(limit)
    txt = urlMonkey.getURL(url)
    return map(lambda x: reformat(x), re.findall('<Text.*?>(.*?)</Text>', txt))

def isDisambiguationPage(title):
    txt = getText(title)
    #print txt
    return 'disambiguation' in '#'.join(getCategories(title)).lower()

def getLinks(title, shelve_only=False):
    #no reformatting except spaces
    title = '_'.join(title.strip().split(' '))
    txt = getText(title, shelve_only=shelve_only)
    links = re.findall('\[\[(.+?)\]\]', txt)
    res = []
    for l in links:
        if ':' in l:
            continue
        res.append(l.split('|')[0])
    return map(lambda x: '_'.join(x.split(' ')), res)
    

def isMissing(title, rfmt=reformat):
    txt = getText(title, rfmt=rfmt)
    return 'missing=\"' in txt

def getInfo(title):
    title = reformat(title)
    url = BASEURL+'?action=query&format=xml&titles='+title+'&prop=info&redirects'
    txt = urlMonkey.getURL(url)
    return re.findall('pageid=\"(.+?)\"', txt)

def isPerson(title):
    #title = reformat(title)
    title = '_'.join(title.split(' '))
    url = BASEURL+'?action=query&format=xml&titles='+title+'&prop=categories&cllimit=45&redirects'
    txt = urlMonkey.getURL(url)

    cats = re.findall('title=\"Category:(.+?)\"', txt)
    res = False
    for c in map(lambda x: x.lower(), cats):
        res = (res or
               'births' in c or
               'deaths' in c or
               'person' in c)
    return res

def listCategs(prefix):
    prefix = '%20'.join(prefix.split(' '))
    url = BASEURL+'?action=query&format=xml&list=allcategories&acprefix='+prefix+'&aclimit=500'
    res = []
    
    txt = urlMonkey.getURL(url)
    res.extend(re.findall('preserve\">(.*?)</c>', txt))
    
    cont = re.search('query-continue><allcategories acfrom=\"(.*?)\"', txt)
    while (cont != None):
        txt = urlMonkey.getURL(url+'&acfrom='+'%20'.join(cont.group(1).split(' ')))
        res.extend(re.findall('preserve\">(.*?)</c>', txt))
        cont = re.search('query-continue><allcategories acfrom=\"(.*?)\"', txt)
    res.extend(re.findall('preserve\">(.*?)</c>', txt))
    return res

def isRedirect(title):
    txt = getText(title)
    #print txt[:600]
    fr = re.search('<r from.+?to=\"(.+?)\" /></redirects', txt)
    shr = re.search('#REDIRECT \[\[(.+?)\]\]', txt)
    if fr != None:
        fr = fr.group(1)
        return True, fr
    elif shr != None:
        shr = shr.group(1)
        return True, shr
    else:
        return False, None
    

def queryInterface(title, depth=0):
    url = ''.join(['http://en.wikipedia.org/w/index.php?title=Special:Search&search=',
                   '+'.join(cgi.escape(title).split(' '))])
    txt = urlMonkey.getURL(url)
    try:
            wtitles = [re.search('<title>(.+?) - Wikipedia', txt).group(1)]
            didyoumean = []
    except:
            return []
    
    if 'search results' in wtitles[0].lower():
        print 'hit search page'
        wtitles = re.findall('href=\"/wiki/(.+?)\"', txt)
        wtitles = filter(lambda x: ':' not in x and '#' not in x and x!='Main_Page', wtitles)
        
        didyoumean = re.search('Did you mean:.+?search=(.+?)[&\"]', txt)
        if didyoumean != None:
            print 'hit search suggestion'
            didyoumean = re.sub('\+', ' ', didyoumean.group(1))
            if depth < 2:
                didyoumean = queryInterface(didyoumean, depth+1)
            else:
                return wtitles+[didyoumean]
        else:
            didyoumean = []
    else:
        print 'hit direct match'
            
    return wtitles+didyoumean

def getCatMembers(cat):
        if 'Category:' not in cat:
                cat = 'Category:' + cat

        cont = re.compile('cmcontinue=\"(.+?)\"')
        titles = re.compile('title=\"(.+?)\"')
                
        cat = re.sub(' ', '_', cat)
        url = (BASEURL+'?action=query&format=xml&list=categorymembers&cmtitle='+
               cat+'&cmlimit=max')
        txt = urlMonkey.getURL(url)

        furl = url
        res = titles.findall(txt)

        m = cont.search(txt)
        while m != None:
                url = furl+'&cmcontinue='+m.group(1)
                txt = urlMonkey.getURL(url)
                res += titles.findall(txt)

                m = cont.search(txt)
        
        return res

def fastGetCats(title):
        txt = getText(title, shelve_only=True)
        return re.findall('(Category:.+?)\]', txt)

def getRedirect(title):
        b, r = isRedirect(title)
        if b: return re.sub(' ', '_', r)
        else: return re.sub(' ', '_', title)
    
if __name__ == '__main__':
    #print getSearchSuggestions('Constant%20Volney')
    #print isDisambiguationPage('Abatement')
    #print getText('2001:_A_Space_Odyssey')
    #print getText('2001:_A_Space_Odessey')
    #getInfo('2001:_A_Space_Odessey')
    a = '2001: A Space Odyssey'
    print getText(a)
    print getCategories(a)
    print isPerson(a)
    
