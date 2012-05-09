import math
import tools
import re

class Cache:
    def __init__(self, name, d):
        self.name = name
        self.d = d
CACHE = Cache('',None)

NAME = None

def getCombinedScores(arttxt, title_txts, comp, occp, wt=True, vt=True):
    wordsa = map(lambda x: x.lower(), tools.splitToWords(arttxt))

    global NAME

    res = []
    for name, text in title_txts:
        NAME = name
        score = 0

        wordsb = map(lambda x: x.lower(), tools.splitToWords(text))
        score1 = comp(wordsb, wordsa) #reverse

        score2 =  getKeywordVectorScore(arttxt, name, text, occp)
        score2 = sum(score2.values())
        
        if wt:
            score += score1*100
        if vt:
            score += score2

        res.append((name, score))
        
    res.sort(key=lambda x: x[1], reverse=True)
    return res

def getProximityScores(arttxt, title_txts, comp):
    '''to be used to calculate a specific meassure using comp'''
    wordsa = map(lambda x: x.lower(), tools.splitToWords(arttxt))

    global NAME
    NAME = title_txts[0][0]

    res = []
    for name, text in title_txts:
        wordsb = map(lambda x: x.lower(), tools.splitToWords(text))
        #print set(wordsa) & set(wordsb)
        res.append((name, comp(wordsa, wordsb)))
    res.sort(key=lambda x: x[1], reverse=True)
    return res


def getDiceScore(words1, words2):
    s1 = set(words1)
    s2 = set(words2)
    return 2*len(s1&s2) / (len(s1)+len(s2))

def getJaccardScore(words1, words2):
    s1 = set(words1)
    s2 = set(words2)
    return len(s1&s2) / len(s1|s2)

####TFIDF:
def getTFScore(wordsa, wordsb, dfd, N, fnD, params, occp=None):
    da = fnD(wordsa, params, occp=occp)
    db = fnD(wordsb, params, occp=occp)
    getTFIDFVector(da, dfd, N)
    getTFIDFVector(db, dfd, N)

    try:
        res = tools.getCosOfDictVectors(da, db)
    except:
        #print 'Exception at', NAME
        res = 0
    return res


def getTFIDFVector(wordDF,dfd, N):
    for w in wordDF.keys():
        if w in wordDF:
            #add in dfd
            if w not in dfd:
                dfd[w] = 1
            #compute tfidf
            try:
                wordDF[w] = wordDF[w] * math.log(N/dfd[w])
            except OverflowError:
                print 'OverflowError'
                continue


def getTFIDFd(words, occp=None):
    d = dict()
    for w in words:
        if w in d:
            d[w] = d[w]+1
        else:
            d[w] = 1
    return d

def getWTFIDFd(words, params, occp, seenlimit=3):
    d = dict()
    ow = set()
    c = 0

    seen = dict()

    limit = params.getFirstWordsLim()
    for w in words:
        if w not in seen:
            seen[w] = 1
        else:
            seen[w]+=1

        if seen[w] > seenlimit:
            continue

        if w in d:
            d[w] = d[w]+1*(limit-c+1)*math.log(limit-c+math.e)
        else:
            d[w] = 1*(limit-c+1)*math.log(limit-c+math.e)


        #years
        if tools.isNumeric(w) and len(w) == 4:
            w = re.sub('o', '0', w)
            w = re.sub('i', '1', w)
            w = re.sub('G', '6', w)
            if w not in d:
                d[w] = 1
                
            if c < limit and w not in ow:
                d[w] *= params.getYearOw()
                ow.add(w)
                #print d[w], w

        #occupations
        if c < limit:
            if w in occp and w not in ow:
                d[w] *= params.getOccOw()
                ow.add(w)
                #print words[:2], d[w], w

        #title words only
        if c < params.getTitleWordsLim():
            if w not in ow:
                d[w] *= params.getTitleWordsOw()
                ow.add(w)
                #print d[w], w
        
        if c < limit:
            c+=1
        #d[w] = int(d[w])
        #print ''
    return d

def fixYear(year):
    year = re.sub('o', '0', year)
    year = re.sub('i', '1', year)
    year = re.sub('G', '6', year)
    year = re.sub('[Ss]', '5', year)
    return year

def isCapitalized(word):
    return word[0] in 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'

def getKeywordScore(art_text, cand_title, cand_text, occp):
    score_dict = dict()
    #links = re.findall('\[+(.+?)\]', cand_text)
    title_words = cand_title.lower().split('_')
    years = (re.findall('Category: *(\d+).+births', cand_text) +
             re.findall('Category: *(\d+).+deaths', cand_text))

    for year in years:
        score_dict[year] = 0
    
    '''while len(links) != 0:
        l = links.pop(0)
        words = tools.splitToWords(l)
        if len(words) > 1:
            links = links+words
        else:
            l = l.lower()
            score_dict[l] = 0
    '''
    
    for word in tools.splitToWords(cand_text)[:150]:
        if word in occp:
            score_dict[word] = 0

        ''' elif isCapitalized(word):
            word = word.lower()
            if word in score_dict:
                score_dict[word] = 0
        '''

    nyear = 0
    nword = 0
    for word in tools.splitToWords(art_text)[:150]:
        nword+=1
        if tools.isNumeric(word):
            year = fixYear(word)
            nyear += 1
            if year in score_dict and nyear < 4:
                score_dict[year] = 100
            elif nyear < 3:
                for wyear in years[:4]:
                    try:
                        iwyear = int(wyear)
                        iyear = int(year)
                        if math.fabs(iwyear-iyear) < 10:
                            score_dict[year] = 80
                    except:
                        continue
        elif nword < 20 and word.lower() in title_words:
            score_dict[word.lower()] = 80
        elif word in occp:
            if word in score_dict:
                score_dict[word] = 20
        '''elif isCapitalized(word):
            word = word.lower()
            if word in score_dict:
                score_dict[word] = 10
        else:
            word = word.lower()
            if word in score_dict:
                score_dict[word] = 15'''
            
    score = 0
    for k in score_dict.keys():
        score += score_dict[k]
    #print cand_title, sorted(score_dict.items(), key=lambda x: x[1], reverse=True)
    return score

def getKeywordCandids(art_text, title_texts, occp):
    res = []
    for title, text in title_texts:
        res.append((title, getKeywordScore(art_text, title, text, occp)))
    res.sort(key=lambda x: x[1], reverse=True)
    return res
                  
def getKeywordVectorScore(art_text, cand_title, cand_text, occp):
    score_dict = dict()
    title_words = cand_title.lower().split('_')
    years = (re.findall('Category: *(\d+).+births', cand_text) +
             re.findall('Category: *(\d+).+deaths', cand_text))

    for year in years:
        score_dict[year] = 0
    
    
    for word in tools.splitToWords(cand_text)[:150]:
        if word in occp:
            score_dict[word] = 0

    nyear = 0
    nword = 0
    for word in tools.splitToWords(art_text)[:150]:
        nword+=1
        if tools.isNumeric(word):
            year = fixYear(word)
            nyear += 1
            if year in score_dict and nyear < 4:
                score_dict[year] = 100
            elif nyear < 3:
                for wyear in years[:4]:
                    try:
                        iwyear = int(wyear)
                        iyear = int(year)
                        if math.fabs(iwyear-iyear) < 10:
                            score_dict[year] = 80
                    except:
                        continue
        elif nword < 20 and word.lower() in title_words:
            score_dict[word.lower()] = 80
        elif word in occp:
            if word in score_dict:
                score_dict[word] = 20
            
    score = {'years':0, 'titles':0, 'occp':0, 'other':0}
    for k in score_dict.keys():
        if k in years:
            score['years']  += score_dict[k]
        elif k in title_words:
            score['titles'] += score_dict[k]
        elif k in occp:
            score['occp']   += score_dict[k]
        else:
            score['other']  += score_dict[k]

    #print cand_title, sorted(score_dict.items(), key=lambda x: x[1], reverse=True)
    return score

def getKeywordVectorCandids(art_text, title_texts, occp):
    res = []
    for title, text in title_texts:
        res.append((title, getKeywordVectorScore(art_text, title, text, occp)))
    res.sort(key=lambda x: x[1], reverse=True)
    return res
