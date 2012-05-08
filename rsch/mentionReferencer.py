import serialize as ser
import get_persons as get_p
import tools

BLACKLIST = ['in', 'a','the','of','or','an','among','is', 'at', 'for', 'out', 'all','and', 'from', 'lat', 'c', 'quot', 'gr', 'fr', 'to']

def getReferences(arts):
    c = 0
    for a in arts[:200]:
        a['directly_mentioned_in'] = []
        for b in arts:
            if 'direct_mention_refs' not in b:
                b['direct_mention_refs'] = []
                
            if a['name'].lower() in b['txt'].lower():
                b['direct_mention_refs'].append(a)
                a['directly_mentioned_in'].append(b)
        print c
        c+=1

def printRefs(arts, fr, to):
    for a in arts[fr:to]:
        print a['name'], 'TO'
        if 'direct_mention_refs' in a:
            for b in a['direct_mention_refs']:
                print '\t', b['name']

        print 'actual refs', repr(a['ref'])
        
        if 'directly_mentioned_in' in a:
            print a['name'], 'FROM'
            for b in a['directly_mentioned_in']:
                print '\t', b['name']            

def getWordDict(arts):
    d = dict()
    for a in arts:
        for w in tools.splitToWords(a['txt'].lower()):
            if w not in d:
                d[w] = set()
            d[w].add(a['id'])
    return d

def getDirectMentions(arts, wd):
    ad = dict()
    for a in arts:
        ad[a['id']] = a
        
    for a in arts:
        words = tools.splitToWords(a['name'].lower())
        
        c = 0
        match = set()
        for w in words:
            c+=1
            if w in wd:
                match = set(wd[w])
                break
            
        for w in words[c:]:
            if w in wd:
                match &= wd[w]

        a['direct_refs'] = []
        for m in list(match):
            a['direct_refs'].append((ad[m]['name'], ad[m]['id']))
            
def getOffsetWordDict(arts):
    d = dict()
    for a in arts:
        words = tools.splitToWords(a['name'].lower())
        for w in words:
            if w in BLACKLIST:
                continue
            
            if w not in d:
                d[w] = []
            d[w].append(a['id'])
    return d

def getOffsetDirectMentions(arts, wd=None, wlen=3):
    ad = dict()
    for a in arts:
        ad[a['id']] = a

    if wd == None:
        wd = getOffsetWordDict(arts)
        
    artcount=0        
    for a in arts:
        words = tools.splitToWords(a['txt'].lower())
        window = words[:wlen]

        a['direct_refs'] = set()

        for w in words[wlen:]+['']:
            matchd = dict()
            for word in list(set(window)):
                if word in wd:
                    for aid in set(wd[word]):
                        if aid not in matchd:
                            matchd[aid] = 1
                        else:
                            matchd[aid] += 1
                
            for m in matchd.keys():
                txtwords = filter(lambda x: x not in BLACKLIST, tools.splitToWords(ad[m]['name']))
                namelen = len(txtwords)
                if namelen <= matchd[m] or matchd[m] == wlen:
                    a['direct_refs'].add((ad[m]['name'], ad[m]['id']))
                    #print namelen, matchd[m], ad[m]['name']
                    
            if len(window) > 0:
                window.pop(0)
            window.append(w)
        print 'at', artcount, 'from', len(arts)
        artcount+=1
                
            
def createTestPage(arts, out, lim=300):
    w = open(out, 'w')
    w.write('<html><body><table border="1"><tr><th>Name</th><th>Direct References</th><th>Actual References</th></tr>\n')
    for a in arts[:lim]:
        w.write('<tr><td>')
        w.write(a['name'])
        w.write('<td>' + repr(a['direct_refs'])+'</td>')
        w.write('<td>' + repr(a['ref']) + '</td></tr>\n')
    w.write('</table></body></html>')
    w.close()

def getWikiDict(wtitles):
    d = dict()
    for wt in wtitles:
        words = wt.split('_')
        for w in words:
            w = w.lower()
            if w not in d:
                d[w] = [wt]
            else:
                d[w].append(wt)
    return d

def getWikiCandidates(wdict, title):
    words = tools.splitToWords(title.lower())
    first = True
    candids = set()
    
    for w in words:
        if w in wdict:
            if first:
                candids |= set(wdict[w])
                #first = False
            else:
                candids &= set(wdict[w])
                
    if len(candids) == 0:
        print 'no match for', title
        
    return list(candids)
