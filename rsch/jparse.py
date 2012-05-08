import re
import tools
from tools import getArticleFromReference, getClosestReference
import pickle

#global regex compilers
#makes it faster lathough they are unnecessary
pname = re.compile('<h1 class=\"articleName\">(.+?)</h1>') #for names
pref = re.compile('<div class=\"seealso.+?title=\"(.+?)\">.+?<a href=\"/.+?/(.+?)\.html\"') #for references

#bref = re.compile('s?S?ee [A-Z][.a-z]+|also [A-Z][a-z]+|s?S?ee [A-Z 0-9]+|also [A-Z 0-9]+')
#bref = re.compile('see\W.{100}(?si)',)
bref = re.compile('see\W.{0,200}?\)(?si)|see\W.{0,200}?;(?si)|see\W.{0,200}?\.(?si)')

#parsing helper functions

def getName(path):
    f = open(path)
    txt = f.read()
    f.close()
    return getArticleName(txt)

def getArticleName(txt):
    '''return #N/A on failure'''
    m = pname.search(txt)
    if m == None:
        return '#N/A'
    else:
        return m.group(1)

def getPureArticle(txt):
    '''
    removes divs with google data and such
    removes html tags
    '''
    
    txt = re.sub('^.+?<p><b><u>(?s)', '<p><b><u>', txt) #clean front
    
    #clean back | clean article name | clean divs, clean tags
    txt = re.sub('<div class=\"content.*?</div>(?s)|<p><b><u>.*?\n|<div.+?/div>(?s)|<.+?>|End of Article.*(?s)', '', txt) 

    #these slow things down. a bit
    #txt = re.sub('<div.+?/div>(?s)', '', txt) #clean divs
    #txt = re.sub('<.+?>', '', txt) #clean tags

    return txt

def getReferences(txt):
    return pref.findall(txt)

def getBRef(txt):
    return bref.findall(txt)

def parse(txt, c):
    d = dict()
    d['name'] = getArticleName(txt)
    d['txt'] = getPureArticle(txt)
    #d['ref'] = getReferences(txt)
    d['bref'] = getBRef(d['txt'])
    d['id'] = c
    return d

def parseAll(path, start, end):
    ds = []
    for c in xrange(start, end+1):
        f = open(path+str(c)+'.html', 'r')
        txt = f.read()
        f.close()

        ds.append(parse(txt, c))
        if c % 1000 == 0:
            print c

    return ds
    

if __name__ == '__main__':

    res = parseAll('../data/raw/11/', 0, 36265)
    f = open('11_clean.pkl', 'w')
    pickle.dump(res, f)
    f.close()
    
    '''    f = open('titles')
    titles = [x[:-1] for x in f.readlines()]
    f.close()

    f = open('titlewords')
    ttlw = [x[:-1] for x in f.readlines()]
    f.close()

    w = open('seealsograph', 'w')
    
    for c in xrange(0, 36266):
        r = open('../data/raw/11/'+str(c)+'.html', 'r')
        txt = r.read()
        r.close()
        
        d = parse(txt)
        ref = getArticleFromReference('#'.join(d['bref']))
        #ref2 = set()
        #for r in ref:
        #    ref2 = ref2 | set(r.split(' '))
        #ref = list(set(ref) | ref2)
        #titles.sort()
        #print c, d['name'], d['bref']
        res = []
        for r in ref:
            #print r, getClosestReference(r, titles)
            #print r, tools.getContainingTitles(r, titles)
            #print c, r, tools.matchRefToIndex(r, ttlw, titles)
            #print c, r
            matched = tools.matchRefToIndex(r, ttlw, titles)
            res.extend(map(lambda x: x[0], matched))

        w.write(str(c))
        w.write(' ')
        for r in res:
            w.write(str(r))
            w.write(' ')
        w.write('\n')
        print c
    w.close()
    '''

        


    
