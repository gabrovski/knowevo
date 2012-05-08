import bisect
import re
import math

ROMAN = 'IVXLCDM' #dont sort this!!!!

def isNumeric(a):
    return reduce(lambda i, x: ('0' <= x and x <='9') or i, a, False)

def matchRefToIndex(ref, titlewords, titles):
    res = []
    ttl = matchRefToTitle2(ref, titlewords, titles)
    for t in ttl:
        res.append((titles.index(t), t))
    return res

def matchRefToTitle(ref, titles, delim=',;'):
    #main function
    #Algortihm:
    #- split each ref by delim
    #- get intersection of containing titles to candid's parts
    #- if the itnersction is not empty
    #  * choose the closes title using leveshtein
    #  * make sure to ignore (...) in the intersection
    #- else
    #  * treat each candid part as a single reference
    #  * if set of containing articles is empty just use levehnstein directly
    #  * choose title from the set using levenshtein
    #  * ignore (...)
    #  * compare that method to the simply using levehnstein
    #  * picke whichever works best
    #- return list of matched titles

    res = []
    parts = re.split(delim, ref)
    con = set(getContainingTitles(parts[0], titles))
    for p in parts[1:]:
        con = con & set(getContainingTitles(p, titles))
    if len(con) != 0:
        res.extend(getClosestReference(ref, list(con)))
    #else:
    if True:
        score = 0
        for p in parts:
            if p == 'OF' or p =='AND':
                score = score + 2
                continue
            con = getContainingTitles(p, titles)
            if len(con) != 0:
                lev = getClosestReference(p, con)
                score = score + lev[0][1]
                res.extend(lev)
            else:
                lev = getClosestReference(p, titles)
                score = score + lev[0][1]
                res.extend(lev)

        alt = getClosestReference(ref, titles)
        if alt[0][1] <= score:
            res = alt
            
    return list(set(res))

def matchRefToTitle2(ref, titlewords, titles):
    refwords = []
    words = [x for x in re.findall('\w+', ref) if x != '']
    score = 0
    for w in words:
        t = getClosestReference(w, titlewords)[0]
        score = score + t[1]
        refwords.append(t[0])
    score = score + len(words)-1
        
    t = getClosestReference(ref, titlewords)[0]
    #print refwords, t
    if score >= t[1]:
        refwords = [t[0]]

    articles = set(getContainingTitles(refwords[0], titles))
    for rw in refwords[1:]:
        articles = articles & set(getContainingTitles(rw, titles))

    if len(articles) == 0:
        refs = ref.split(',') #exclude subportion
        if len(refs) > 1: 
            ref = refs[0] 
            articles = matchRefToTitle2(ref, titlewords, titles)
        else: #no subportion
            ref = t[0] #straight up closest match
            articles = matchRefToTitle2(ref, titlewords, titles)

    closest = getClosestReference(ref, list(articles))
    return map(lambda x : x[0], closest)


def getTitleWords(titles):
    words = set()
    for t in titles:
        words = words | set([x for x in re.findall('[A-Z0-9]+', t) if x != ' '])
    return list(words)
    
def getLevenNoPar(a, b):
    '''remove (...) from b'''
    b = re.sub(' \(.*', '', b)
    b = re.sub('[,;]', '', b)
    return getLevenshtein(a, b)

def getContainingTitles(candid, titles):
    res = []
    for t in titles:
        try:
            t.index(candid)
            res.append(t)
        except ValueError:
            continue
    return res

def getClosestReference(candid, titles, distanceFunction=getLevenNoPar):
    if candid in titles:
        return [(candid, 0)]
    else:
        res = []
        for i in xrange(len(titles)):
            res.append((titles[i], distanceFunction(candid, titles[i])))
        res.sort(key=lambda x: x[1])

        #return a lit of entities with the same score
        i = 0
        while i < len(res) and res[0][1] == res[i][1]:
            i = i+1
        return res[:i]
            

def getLevenshtein(a, b):
    '''
    http://en.wikipedia.org/wiki/Levenshtein_distance
    '''
    lena = len(a)
    lenb = len(b)
    d = [[0 for x in xrange(lenb+1)] for y in xrange(lena+1)]
    for i in xrange(1, lena+1):
        d[i][0] = i
    for j in xrange(1, lenb+1):
        d[0][j] = j

    for i in range(1, lena+1):
        for j in range(1, lenb+1):
            if a[i-1] == b[j-1]:
                d[i][j] = d[i-1][j-1]
            else:
                insr = d[i][j-1]+1
                delt = d[i-1][j]+1
                repl = d[i-1][j-1]+1
                d[i][j] = min(insr, delt, repl)
    return d[lena][lenb]

def isRomanNumeral(txt):
    if len(txt) == 0:
        return False
    try:
        prev = ROMAN.index(txt[0])
        for l in txt[1:]:
            i = ROMAN.index(l)
            if prev < i:
                return False
            prev = i
    except ValueError:
        return False
    return True

def getArticleFromReference(ref):
    words = [w for w in re.findall('[\w,;]*', ref) if w != '']
    res = set()
    prev = None
    for w in words:
        if w[0] <= '9' or isRomanNumeral(w):
            continue
        cap = 0
        for let in w:
            if not (('a' <= let and let <= 'z') or let in [',',';']):
                cap = cap + 1
        if cap > 1:
            #res.add(w)
            
            if prev != None:
                prev = prev + ' '+w
            else:
                prev = w
        elif prev != None:
            #res = res | set([x for x in re.split('[;,]', prev) if x != ''])
            dlm = '['
            if ';' in prev:
                #res = res | set([x for x in prev.split(';') if x != ''])
                dlm = dlm+';'
            if len([x for x in prev if x == ',']) > 1:
                dlm = dlm+','
                #res = res | set([x for x in prev.split(',') if x != ''])
            dlm = dlm+'#]'
            res = res | set([x for x in re.split(dlm, prev) if x != ''])
            #res.add(prev)
            #print len([x for x in prev if x == ','])
            prev = None
    if prev != None:
        res.add(prev)
    return list(res)


#copy to module that uses it. do not import jparse in this module!
def getJArticleName(i):
    f = open('../data/raw/11/'+str(i)+'.html')
    txt = f.read()
    f.close()
    d = jparse.parse(txt)
    return d['name']

def getCosOfDictVectors(a, b):
    '''a and b are dictionaries'''
    ab = 0
    for k in a.keys():
        if k in b:
            ab = ab + a[k]*b[k]

    avals = a.values()
    aa = sum(map(lambda x,y: x*y, avals, avals))

    bvals = b.values()
    bb = sum(map(lambda x,y: x*y, bvals, bvals))
    #print aa, bb, ab
    #print ab, (math.sqrt(aa)*math.sqrt(bb))
    return ab/(math.sqrt(aa)*math.sqrt(bb))
    

def getCosOfVectors(a, b):
    ab = sum(map(lambda x,y: x*y, a, b))
    la = sum(map(lambda x,y: x*y, a, a))
    lb = sum(map(lambda x,y: x*y, b, b))

    return (ab+0.0)/math.sqrt(la)/math.sqrt(lb)

def removeDuplicates(A):
    A.sort()
    return [A[i] for i in xrange(len(A)) if A[i] not in A[i+1:]]

def bsearch(A, query, low, high):
    if high < low:
        return -1
    
    mid = (high+low)/2
    if A[mid] > query:
        return bsearch(A, query, low, mid-1)
    elif A[mid] < query:
        return bsearch(A, query, mid+1, high)
    else:
        return mid

def lowerList(A):
    return map(lambda x: x.lower(), A)

def sortAndJoinWordlist(A, splitdelim='_', delim='_'):
    res = []
    for w in A:
        parts = lowerList(w.split(splitdelim))
        parts.sort()
        res.append(delim.join(parts))
    return res

def splitWordList(A, delim='_'):
    return [word.split(delim) for word in A]

def splitToWords(txt):
    '''return word list. split hyphenated words in two'''
    return [x for x in re.findall('[a-z0-9]*(?i)', txt) if x != '']

def findBestSuffixMatchLength(words, candid):
    ''' works with prefixes not suffixes'''
    max = 0
    lencandid = len(candid)
    for w in words:
        c = 0
        for i in xrange(min(len(w), lencandid)):
            if w[i] == candid[i]:
                c = c + 1
            else:
                break
        if c > max:
            max = c
    return max

def findMaxCombinedSuffixLength(wordslist, candidlist, delim='_'):
    '''max sum of matched suffixes across all words in the query and a title'''
    max = 0
    res = None
    for ws in wordslist:
        curr = 0
        for c in candidlist:
            curr = curr+findBestSuffixMatchLength(ws, c)
        if curr > max:
            max = curr
            res = ws
    return delim.join(candidlist), delim.join(res), max

def findLongestSubstringLength(base, candid):
    m = [[0 for x in xrange(len(candid))] for y in xrange(len(base))]
    res = 0
    for i in xrange(len(base)):
        for j in xrange(len(candid)):
           if base[i] == candid[j]:
               if i == 0 or j == 0:
                   m[i][j] = 0
               else:
                   m[i][j] = m[i-1][j-1]+1
               if m[i][j] > res:
                   res = m[i][j]
    return res

def findBestMatchBySubstring(words, candid):
    '''
    assume words are sorted
    return best matching string
    match by character
    very inefficient!
    '''
    mi = -1
    mj = -1

    for i in xrange(len(words)):
        j = findLongestSubstringLength(words[i], candid)
        if mj < j:
            mj = j
            mi = i
            
    return mi, mj


def totalSubstringLength(word, candid, delim='_'):
    '''find all substrings for each word and return the max combined length'''
    words = word.split(delim)
    parts = candid.split(delim)
    total = 0
    for p in parts:
        i, c = findBestMatchBySubstring(words, p)
        total = total + c
    return total

def findBestMatch(words, candid):
    '''
    assume words are sorted
    bsearch to appropriate left insertion index
    return best matching string
    match by character
    '''
    i = bisect.bisect_left(words, candid)

    #check from 1 behind
    if i != 0:
        i = i -1

    c = 0
    mi = -1
    mj = -1

    wlen = len(words)
    while c < 3 and i < wlen:
        j = 0
        lena = len(words[i])
        lenb = len(candid)
        while j < lena and j < lenb and words[i][j] == candid[j]:
            j = j+1
        c = c+1
        if mj < j:
            mj = j
            mi = i
        i = i +1
    return mi, mj

def findMatchingWords(w1, w2, delim='_'):
    '''match by whole words'''
    p1 = w1.split(delim)
    p1.sort()
    p2 = w2.split(delim)
    p2.sort()

    i = 0
    m = 0
    len1 = len(p1)
    len2 = len(p2)

    while(i < len1 and i < len2):
        if p1[i] == p2[i]:
            m = m + 1
        i = i+1
    return m

class MatchWords:
    def __init__(self):
        self.words = []
        self.map = dict()

    def setMatchWords(self, words, delim='_'):
        self.words = []
        for i in xrange(len(words)):
            w = words[i]
            parts = w.split(delim)
            parts.sort()
            nw = delim.join(parts)
            self.words.append(nw)
            self.map[nw] = i
        self.words.sort()
        
    def unsetMatchWords(self):
        self.words = []
        self.map.clear()

    def findMatchingWords(self, w1, sortedparts, delim='_'):
        return len(set(w1.split(delim)) & set(sortedparts))

    def findBestMatchByWord(self, candid, delim='_'):
        sortedparts = candid.split(delim)
        sortedparts.sort()
        i = 0
        
        c = 0
        mi = -1
        mj = -1

        wlen = len(self.words)
        while i < wlen:
            j = self.findMatchingWords(self.words[i], sortedparts)
            c = c+1
            if mj < j:
                mj = j
                mi = i
            i = i +1
            
        return self.map[self.words[mi]], mj

if __name__ == '__main__':
    #print findBestMatch(['WORSHIP_ie_worth_ship_O_Eng_weo'], 'WORSLEY')
    #print findLongestSubstringLength("sdasdsdasdajjhkfl", "weqweqsdajjh")
    #print getCosOfVectors([-1,1],[1,0])
    print getLevenshtein('Sunday', 'Saturday')
