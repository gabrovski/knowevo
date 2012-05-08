import tools
import wiki
import pickle
import bisect
import re
import math
import jparse
import time
import sys
import bing
import art_measure as a_m
import htmllib
import serialize as ser
import os
from params import PARAMS

MIN_VAL = 0
COUNT = 0

AVG = 0.0
N = 0.0
VAR = 0.0

DISAMBIG_PATTERN = re.compile('\[\[(.+?)\]')

def unescape(s):
    p = htmllib.HTMLParser(None)
    p.save_bgn()
    p.feed(s)
    return p.save_end()

def reformat(title):
    return unescape(re.sub(' ', '_', title.lower()))

def removeDisambigCandidates(wt):
    if wiki.isDisambiguationPage(wt):
        print 'disambig', wt
        dtitles = wiki.getLinks(wt)
        return set(dtitles)

def addBingLinks(q, res, limit=5):
    links = filter(lambda x: 'en.wikipedia.org/wiki/' in x,bing.directQuery(q, limit))
    #print links
    s = set()
    links = list(map(lambda x: x.split('#')[0], links))
    
    #links = links[:limit]
    #print links
    res = res | set(map(lambda x: x.split('/wiki/')[1], links))
    return res


def getNeighbors(artd, wikititles, range=3, BING=True, WIKIBOT=True, params=PARAMS):
    #return titles from the vicinity in the range
    title = artd['name']
    
    #put last name last
    title = ' '.join(artd['name'].split(',')[::-1]).strip()
    #title = reformat(title)
    
    print title

    
    index = bisect.bisect_left(wikititles, title)
    res = set()
    
    for wt in wikititles[index-range:index+range]:
        if 'isambiguation' not in wt:
            res.add(wiki.reformat(wt))
    
    #bing it
    blim1 = params.getNumWordsQ1()
    blim2 = params.getNumWordsQ2()
    if BING:
        firstwords = '%20'.join(tools.splitToWords(artd['txt'])[:blim1])
        q = 'site%3Awikipedia.org%20'+ re.sub(' \(.*', '', title)+'%20'+firstwords
        res = addBingLinks(q, res)

        #secnod time with more words
        firstwords = '%20'.join(tools.splitToWords(artd['txt'])[:blim2])
        q = 'site%3Awikipedia.org%20'+ re.sub(' \(.*', '', title)+'%20'+firstwords
        res = addBingLinks(q, res)

        #print q
    
        q = 'site%3Awikipedia.org%20'+ re.sub(' \(.*', '', title)
        res = addBingLinks(q, res)

    if WIKIBOT:
        res |= set(wiki.queryInterface(title))
        res |= set(wiki.queryInterface('_'.join(
            [title] +
            tools.splitToWords(artd['txt'])[:blim1])))
        

    #print '\n\n', res, '\n\n'
    
    #wiki suggestions
    res = res | set(wiki.getSearchSuggestions(title, limit=5))

    #wiki search
    res = res | set(wiki.search(title))
    
    #print res
    #deal with disambiguation pages
    artd['missing'] = set()
    artd['disambig'] = set()
    for wt in list(res):
        if 'Talk:' in wt:
            res.add(wt.split('Talk:')[1])
            continue
        if ':' in wt or 'List_of' in wt or 'Category:' in wt:
            continue
        if wiki.isMissing(wt, rfmt=lambda x: x):
            res.remove(wt)
            if wt not in artd['missing']:
                artd['missing'].add(wt)
                res |= set(wiki.getSearchSuggestions(wt))
                print 'miss', wt
                continue
        
        dtitles = removeDisambigCandidates(wt)
        if dtitles != None:
            artd['disambig'].add(wt)
            res.remove(wt)
            res |= dtitles

    res = set(map(lambda x: re.sub(' ', '_', x), list(res)))

    for wt in list(res):
        if re.match('\d\d\d*', wt):
            res.remove(wt)
    #print res
    return list(res)

def getAllWords(wordFreqD):
    return set(wordFreqD.keys())


def updateMinVal(mincos):
    global MIN_VAL
    global COUNT
    
    if MIN_VAL == 0:
        MIN_VAL = mincos
        COUNT = 1
    else:
        MIN_VAL = COUNT*MIN_VAL+mincos
        COUNT = COUNT+1
        MIN_VAL = MIN_VAL/COUNT

def updateVar(val):
    global VAR, N, AVG
    
    AVG = (AVG*N + val) / (N+1)

    VAR = VAR*N+(AVG-val)*(AVG-val)
    VAR = VAR / (N+1)

    N = N+1

def getZScore(val):
    std = math.sqrt(VAR)
    return math.fabs((val-AVG)/std)

N15 = 81697
def matchArticle(artd, wikititles, dfd, occp=None, N=N15, sigma=0.1, params=PARAMS):
    #returns closest article
    #uses word document frequencies as an pproximation (DFD - document freq)
    #from an existing edition
    #for tfidf over the article and wthe wikipedia articles
    #creates tfidf vectors for all articles
    #computes cosines and returns the closest article

    if occp == None:
        occp = loadOccupations()

    global MIN_VAL
    global COUNT

    if 'candids' not in artd:
        nghb = getNeighbors(artd, wikititles, params=params)
    else:
        nghb = map(lambda x: x[0], artd['candids'])
    artd['nghb'] = nghb
    #print 'nghb'
    #add title as first word
    title_texts = [(n, re.sub('_', ' ', n)+" "+wiki.getText(n)) for n in nghb]

    #remove the empty texts(article not found)
    title_texts = filter(lambda x: x[1] != x[0]+' ', title_texts)

    '''
    #print 'txt'
    fn = lambda x, y: a_m.getTFScore(x,y,dfd,N, a_m.getWTFIDFd, params, occp=occp)
    candids = a_m.getProximityScores(artd['name']+" "+artd['txt'], title_texts, fn)
    #print 'distance'
    '''

    candids = a_m.getKeywordVectorCandids(artd['name']+" "+artd['txt'], title_texts, occp)
    
    if candids == []:
        return '#NA'

    #print candids
    artd['candids'] = candids
    
    res = candids
    '''for c in candids[1:]:
        if candids[0][1] - c[1] < sigma:
            res.append(c)

    print artd['name']
    for r in res:
        print r
    
    mincos = res[len(res)/2][1] #median
    updateMinVal(mincos)

    matchcos = res[0][1]

    updateVar(matchcos)
    
    try:
        z = getZScore(matchcos)
    except:
        z = 0
    
    if matchcos <= MIN_VAL or z > 2:
        #matched = 'weak://'+matched
        pass
    '''
    return map(lambda x: x[0], res)

def matchPickledArticles(arts, wikititles, dfd, N, out, params=PARAMS, start=0):
    w = open(out, 'w')
    occp=loadOccupations()
    
    c = start
    for artd in arts[start:]:
        matched = matchArticle(artd, wikititles, dfd, N=N, occp=occp, params=params)
        artd['matched'] = matched
        w.write(artd['name'])
        w.write('\tTO\t')
        w.write(repr(matched))
        w.write('\n')
        w.flush()
        
        print c, os.getpid()
        c+=1

        if c % 1000 == 0:
            print 'Saving'
            sav = open(out.split('.')[0] +'_v'+str(c%3)+ '.pkl', 'w')
            pickle.dump(arts, sav)
            sav.close()

    w.close()
    sav = open(out.split('.')[0] + '.pkl', 'w')
    pickle.dump(arts, sav)
    sav.close()


def getCandidVariance(arts):
    var = []
    sum = 0
    for a in arts:
        for c, sc in a['candids']:
            sum += sc
            var.append(sc)

    mean = sum / (0.0+len(var))
    variance = 0
    for v in var:
        variance += (mean - v)**2
    return variance

def getTopCandids(art, sdev, sigma):
    if len(art['candids']) == 0:
        return []
    limit = art['candids'][0][1]-sdev*sigma

    res = []
    for c, sc in art['candids']:
        if sc > limit:
            res.append(c)
    return res

def properFilterArts(arts, params=PARAMS):
    
    #iF year and title - its matched
    #iF no year - find first with year and title if there is one.
    #iF diff greater than 80 then it's bad
    
    res = []
    c = 0
    MAX_DIFF = 80
    
    for a in arts:
        matched = []
        first_name, first_score = a['candids'][0]
        if wiki.isDisambiguationPage(first_name):
                print name, 'dis'
                
        else:
            first_sum = sum(first_score.values())
            if first_score['years'] > 0 and first_score['titles'] > 0:
                matched = [wiki.getRedirect(first_name)]
                a['matched'] = matched
                res.append(a)
                continue

        #not first match        
        for name, score in a['candids'][1:]:
            if wiki.isDisambiguationPage(name):
                print name, 'dis'
                continue
            
            if score['years'] > 0 and score['titles'] > 0:
                asum = sum(score.values())
                if first_sum - asum < MAX_DIFF:
                    a['matched'] = [wiki.getRedirect(name)]
                    res.append(a)
                    break
                else:
                    print a['name'],'DIFFBIG:',asum,name
                    break
    return res
        

def loadOccupations():
    files = ['../data/occp/occupations', '../data/occp/nobility']
    res = set()
    for name in files:
        f = open(name)
        for line in f:
            res.add(line[:-1].lower())
        f.close()
    return res

if __name__=='__main__':
    dfd = ser.loadDict('dicts/15_dfd.pkl')
    wts = ser.getWikiTitles()    
    
    #arts = ser.loadDict('dicts/newcontext_people_9_v2.pkl')
    arts = ser.loadDict('dicts/newcontext_people_15_v1.pkl')
    c = 0
    for a in arts:
        if 'candids' not in a:
            break
        c+=1
    c = 37000
    print 'starting at', c
    
    matchPickledArticles(arts, wts, dfd, N15, 'dicts/newcontext3_people_15.txt', start=c)
    print 'shelve hit', wiki.SHM.hit
    print 'shelve miss', wiki.SHM.miss
    
    ser.saveDict(properFilterArts(arts), 'dicts/newcontext2_people_15_filtered.pkl')


    '''
    f = open('wikipedia/wikititles')
    wikititles = [l[:-1].lower() for l in f]
    f.close()
    wikititles.sort()
    
    f = open('wikipedia/titles')
    titles = [l[:-1] for l in f]
    f.close()

    f = open('mcl/dfd.pkl')
    dfd = pickle.load(f)
    f.close()

    print 'Loading finished'
    matchPickledArticles('../../shared/edition-3/articles.pkl', dfd, 36265, 'dicts/dquery_2_3_wiki_full.txt')
    '''
    




