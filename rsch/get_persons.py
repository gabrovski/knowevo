import wiki
import re
import bing
import pickle
import serialize as ser
import tools

def get_persons(path, out):
    f = open(path)
    arts = pickle.load(f)
    f.close()

    res = []

    w = open(out, 'w')
    for art in arts:
        t = art['name']
        q = 'site:wikipedia.org%20'+ re.sub(' \(.*', '', t)
        links = filter(lambda x: 'en.wikipedia.org/wiki/' in x,bing.query(q, 3))
        try:
            candids = map(lambda x: wiki.reformat(x.split('/wiki/')[1]), links)
            person = False
            for c in candids:
                person = person or wiki.isPerson(c)
            
            if person:
                print t
                res.append(art)
                w.write(t)
                w.write('\n')
            else:
                print '\t', t
        except:
            print 'no match', t
            raise
    w.close()
    return res

def reformatNames(arts):
    res = []
    for a in arts:
        name = filter(lambda x: x!='\n', a['txt'][:30])
        pn = re.search('(.+?\(.*?\))', name)
        if pn != None:
            print pn.group(1)
            res.append(a)
    return res

def reformatNamesComma(arts):
    for a in arts[:100]:
        name = filter(lambda x: x!='\n', a['txt'][:30])
        pn = re.search('(.*?),', name)
        if pn != None:
            print a['name'],'#',pn.group(1), '#', name
        else:
            print a['name']

def getWords(str):
    res = filter(lambda x: x!= '', re.split('[,\.;\"\' \n\r\t\:\?\[\]\{\}\!\(\)]', str))
    mlen = -1
    for r in res:
        if len(r) > mlen:
            mlen = len(r)
    if mlen < 2:
        res = [''.join(res)]

    return res

def getLowerWords(str):
    return map(lambda x: x.lower(), getWords(str))

class Name:
    def __init__(self, art):
        self.orig_name = art['name']
        self.art = art
        art['implied_refs'] = dict()
        self.parse(self.orig_name)

    def parse(self, orig):
        orig = orig.lower()
        if '(' in orig:
            self.last = set(getWords(orig[:orig.index('(')]))
            if ')' in orig:
                self.first = set(getWords(re.search('\((.*?)\)', orig).group(1)))
            else:
                self.first = set(getWords(orig[orig.index('(')+1:]))
            
        else:
            words = orig.split(',')
            self.last = set(getWords(words[0]))
            if len(words) > 1:
                self.first = set(getWords(orig[1]))
            else:
                self.first = set()
                
    def check(self, words, a):
        if len(self.last) == 0:
            return False
        
        if a['id'] in self.art['implied_refs']:
            return False
        
        words = map(lambda x: x.lower(), words)
        if len(set(words) & self.last) == len(self.last):
            self.art['implied_refs'][a['id']] = a
            return True
        return False

def getNamesDict(arts):
    names = map(lambda x: Name(x), arts)
    keys = map(lambda x: '#'.join(sorted(list(x.last))), names)
    
    d = dict()
    for k, v in zip(keys, names):
        d[k] = v

    return d

def findRefs(art, names, lim=7):
    words = []
    for w in getWords(art['txt']):
        w = w.lower()
        #if w in ['was','is','of','','the','a','in', 'and', 'to', 'an', 'at', 'it', 'as']:
        #continue
        
        if len(words) == lim:
            words.pop(0)
        words.append(w)

        for n in names:
            if n.check(words, art):
                print n.orig_name, '#', art['name'], words, '#', n.first, n.last
                words = []

def findRefsWithDict(art, namesd, lim=4):
    words = []
    getKey = lambda x : '#'.join(sorted(x))
    for w in getWords(art['txt']):
        w = w.lower()
        #if w in ['was','is','of','','the','a','in', 'and', 'to', 'an', 'at', 'it', 'as']:
        #continue

        if len(words) == lim:
            words.pop(0)
        words.append(w)

        for i in xrange(len(words)-1):
            key = getKey(words[i:])
            if key in namesd:
                n = namesd[key]
                if n.check(words, art):
                    print n.orig_name, n.art['name'], '#', art['name'], words, '#', n.first, n.last

def test(arts):
    nd = getNamesDict(arts)
    for a in arts:
        findRefsWithDict(a, nd)

def getGraph(arts, out, lim=1000):
    nd = getNamesDict(arts)
    for a in arts:
        findRefsWithDict(a, nd)
    
    arts.sort(key=lambda x: len(x['implied_refs']), reverse=True)

    f = open(out, 'w')
    for a in arts[:lim]:
        f.write(a['name'])
        f.write(a['txt'])
        f.write('\nREFERENCES: ')
        for r in a['implied_refs'].values():
            f.write(r['name'])
            f.write('  ;  ')


BLACKLIST = ['in', 'a','the','of','or','an','among','is', 'at', 'for', 'out', 'all','and']


def getReferenceCandidates(arts):
    d = dict()
    c = 0
    for a in arts:
        print 'at', c
        c+=1
        
        for w in getLowerWords(a['name']):
            if w in BLACKLIST:
                continue
            if w not in d:
                d[w] = []
            d[w].append(a)

    c = 0
    for a in arts:
        print 'at', c
        c+=1

        a['ref_candidates'] = []
        s = set()
        for w in getLowerWords(a['txt']):
            if w in d:
                for cand in d[w]:
                    if cand['id'] not in s:
                        a['ref_candidates'].append(cand)
                        s.add(cand['id'])
    return d

def getActualRefsForArticle(a, w):
    wlinks = set()
    a['wiki_derived_refs'] = []
    for m in a['matched']:
        wlinks |= set(wiki.getLinks(m))

    some = False
    for candid in a['ref_candidates']:
        inter = set(candid['matched']) & wlinks
        if len(inter) > 0:
            print a['name'], 'to', candid['name'], 'on' , inter, 'and', a['ref'], '\n',
            a['wiki_derived_refs'].append(candid)
            
            some = True

    if not some:
        print 'none and', a['ref']
        w.write('<tr><td>')
        w.write(str(a['name']))
        w.write('</td><td>None</td><td>')
        w.write(str(a['ref']))
        w.write('</td></tr>\n')
    else:
        w.write('<tr><td>')
        w.write(str(a['name']))
        w.write('</td><td>')
        w.write(str(set(map(lambda x: x['name'], a['wiki_derived_refs']))))
        w.write('</td><td>')
        w.write(str(a['ref']))
        w.write('</td></tr>\n')
        

def getActualReferences(arts, out):
    if 'ref_candidates' not in arts[0]:
        getReferenceCandidates(arts)

    w = open(out, 'w')
    w.write('<html><body>\n<table>\n')
    w.write('<tr><th>Article Name</th><th>Mention References</th><th>Actual References</th></tr>\n')

    c = 0
    for a in arts[:1000]:
        print 'at', c
        c+=1
        getActualRefsForArticle(a, w)

    w.close()

def testRef():
    e15 = ser.loadDict('dicts/15_wiki_full.pkl')
    p15 = ser.loadDict('dicts/people_ed_15.pkl')
    s = set()
    for p in p15:
        s.add(p['id'])

    ed15 = filter(lambda x: x['id'] in s, e15)
    ser.ed15 = ed15
    getActualReferences(ed15, 'mention_references_15.html')
    


if __name__ == '__main__':
    #get_persons('../../shared/edition-15/articles.pickle', 'current_people.txt')
    #pres = get_persons('../../shared/edition-15/articles.pickle', 'edition_15_people.txt')
    #ser.saveDict(pres, 'dicts/people_ed_15.pkl')
    pass
