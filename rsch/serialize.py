import cPickle
import bisect
import jparse
import sys
import wikiMatcher
import wiki
import os
import mentionReferencer
import re

def makeLocalLink(art):
    PATH = '/afs/northstar.dartmouth.edu/users/a/agabrovs/public_html/sampler/'
    URL = 'http://caligari.dartmouth.edu/~agabrovs/sampler/'
    name = re.sub('[ /\\\\]', '_', art['name'])+'_'+str(art['id'])+'.txt'
    ls = os.listdir(PATH)
    
    if name not in ls:
        w = open(PATH+name, 'w')
        w.write(art['name']+'\n'+art['txt'])
        w.close()
    return '<a href="'+URL+name+'">'+art['name']+'</a>'

def printStats(art):
    if 'name' in art: print art['name']
    if 'matched' in art: print art['matched']
    if 'nghb' in art: print art['nghb']

def findArticle(arts, name):
    return filter(lambda x: name.lower() in x['name'].lower(), arts)

def getPeopleOnly(arts, ptxt):
    nameset = set()
    f = open(ptxt)
    for line in f:
        nameset.add(line[:-1])
    f.close()
    
    parts = filter(lambda x: x['name'] in nameset and 'matched' in x, arts)
    return parts

def getWikiTitles():
    f = open('wikipedia/wikititles')
    wikititles = [l[:-1].lower() for l in f]
    f.close()
    wikititles.sort()
    return wikititles


def loadDict(path):
    f = open(path)
    arts = cPickle.load(f)
    f.close()
    return arts

def saveDict(arts, path):
    f = open(path, 'w')
    cPickle.dump(arts, f)
    f.close()


def doJavaPRank(arts, txthandle, outfile, key='ref', rankkey='rank'):
    serializeGraph(arts, txthandle, k=key)
    pid = os.fork()
    if pid == 0:
        try:
            os.execlp('java', 'java', '-jar', 'pageRank.jar', txthandle, outfile)
            os.exit(0)
        except:
            os.exit(-1)
    else:
        os.waitpid(pid, 0)
        includeRanks(arts, outfile, rankkey)

def old_serializeGraph(arts, out):    
    d = dict()
    for a in arts:
        d[a['id']] = a['count_id']
    
    w = open(out, 'w')
    for a in arts:
        w.write(str(a['count_id']))
        for r in a['ref']:
            w.write(' ')
            try:
                w.write(str(d[r[1]]))
            except:
                print 'No key', r[1]
        w.write('\n')
        print a['id']
    w.close()

def serializeGraph(arts, out, k='ref'):    
    w = open(out, 'w')
    for a in arts:
        w.write(str(a['id']))
        for r in a[k]:
            w.write(' ')
            w.write(str(r[1]))
        w.write('\n')
        print a['id']
    w.close()

def old_deserializePRank(ppath, gpath, out):
    f = open(ppath)
    arts = pickle.load(f)
    f.close()

    d = dict()
    for a in arts:
        d[a['count_id']] = a['name']

    w = open(out, 'w')
    f = open(gpath)
    for line in f:
        id, rank = line[:-1].split(':')
        w.write(d[int(id)])
        w.write(':')
        w.write(rank)
        w.write('\n')
    w.close()
    f.close()

def deserializePRank(arts, gpath, out):
    #f = open(ppath)
    #arts = pickle.load(f)
    #f.close()

    d = dict()
    for a in arts:
        d[a['id']] = a['name']

    arts = []
    f = open(gpath)
    for line in f:
        id, rank = line.split(':')
        arts.append((id, float(rank)))

    arts.sort(key=lambda x: x[1], reverse=True)

    w = open(out, 'w')
    for id, rank in arts:
        w.write(d[id])
        w.write(':')
        w.write(str(rank))
        w.write('\n')
    w.close()
    f.close()


def deserializeWiki(ppath, tpath, out):
    f = open(tpath)
    titles = map(lambda x: x[:-1], f.readlines())
    f.close()

    w = open(out, 'w')
    
    f = open(ppath)
    arts = []
    for line in f:
        id, rank = line.split(':')
        arts.append((id, float(rank)))

    arts.sort(key=lambda x: x[1], reverse=True)

    for id, rank in arts:
        w.write(titles[int(id)-1]) #indexed from 1, not 0
        w.write(':')
        w.write(str(rank))
        w.write('\n')
    
    f.close()
    w.close()

def desMlogs(lpath, ppath, out):
    f = open(ppath)
    arts = pickle.load(f)
    f.close()
    
    f = open(lpath)
    curr = []
    for line in f:
        try:
            id = int(line)
            d = arts[id]
            d['matched'] = curr
            curr = []
            print id
        except:
            if line[0] == '(':
                wtitle = line[2:].split('\'')[0]
                print '\t',wtitle
                curr.append(wtitle)
    f.close()

    f = open(out, 'w')
    pickle.dump(arts, f)
    f.close()

def includeRanks(arts, rpath, k='rank'):
    d = dict() #fast lookup
    for a in arts:
        if 'id' not in a:
            print 'no id!'
            break
        d[int(a['id'])] = a

    f = open(rpath)
    for line in f:
        id, rank = line[:-1].split(':')
        if int(id) not in d:
            continue
        a = d[int(id)]
        a[k] = float(rank)
    f.close()
    

def pickleWiki(tpath, rpath, out):
    f = open(tpath)
    titles = map(lambda x : x[:-1], f.readlines())
    f.close()

    f = open(rpath)
    res = []
    for line in f:
        id, rank = line[:-1].split(':')
        d = {'id':int(id), 'name':titles[int(id)-1], 'rank':float(rank)}
        res.append(d)

    f.close()
    f = open(out, 'w')
    pickle.dump(res, f)
    f.close()


def normalizeRank(arts, norm=100000):
    '''
    think of a pagerank as the articles share of importance in the network
    get the total share of importance, get it ratio and normalize to <norm>
    '''

    total = 0
    for a in arts:
        try:
            total += a['rank']
        except:
            print 'no rank!'
            a['rank'] = 0

    for a in arts:
        a['n_rank'] = a['rank']*(norm/total)
        if str(a['rank']  )== 'NaN':
            a['rank'] = 0.0
            print 'NaN'

    #sort
    arts.sort(key=lambda a: float(a['rank']), reverse=True)
    for c in xrange(len(arts)):
        arts[c]['i_rank'] = c
        arts[c]['i_n_rank'] = norm*(c+0.0)/len(arts)
1
        #return arts    

def include11Ids(arts):
    f = open('../data/links.txt')
    links = f.readlines()
    f.close()

    f = open('dicts/namedict')
    named = pickle.load(f)
    f.close()

    c = 0
    ld = dict()
    for l in links:
        title = l.split('/')[-1:][0].split('.')[0]
        path = named[title]
        
        title = jparse.getName(path)
        if title in ld:
            print 'Already here'
        ld[title] = c
        c+=1
        
    for a in arts:
        a['id'] = ld[a['name']]


def fix11Ref(arts):
    f = open('../data/links.txt')
    links = f.readlines()
    f.close()

    c = 0
    ld = dict()
    for l in links:
        title = int(l.split('/')[-1:][0].split('.')[0])
        ld[title] = c
        c+=1
        
    for a in arts:
        nr = [ (name, ld[html_name]) for name, html_name in a['ref'] ]
        a['ref'] = nr

def fixRefs(arts):
    d = dict()
    for a in arts:
        d[a['name']]=a
    for a in arts:
        res = []
        for name, something in a['ref']:
            name = ' '.join(name.split(' ')[:-1])
            res.append((name, d[name]['id']))
        a['ref'] = res

def includeMatched(arts, m_arts):
    ld = dict()
    for a in arts:
        ld[int(a['id'])] = a
        
    for a in m_arts:
        print a['id']
        ld[int(a['id'])]['matched'] = a['matched']


def switchRefs(old_arts, new_arts):
    ld = dict()
    for a in old_arts:
        ld[a['id']] = a

    for a in new_arts:
        a['ref'] = ld[a['id']]['ref']

    return new_arts

def mergeCorpus(rev_wiki, arts_list):
    d = dict()
    d['rev_wiki'] = rev_wiki
    for arts in arts_list:
        d[arts[0]['edition']] = arts

    for arts in arts_list:
        print 'at', arts[0]['edition']
        for a in arts:
            for m in a['matched']:
                if m not in rev_wiki:
                    continue
                
                wa = rev_wiki[m]
                a_list = wa['editions'][a['edition']]
                res = []
                for false_art in a_list:
                    if false_art['name'] == a['name'] and false_art['wlen'] == a['wlen']:
                        res.append(a)
                    else:
                        res.append(false_art)
                        
                wa['editions'][a['edition']] = res
    return d

def find(arts, ma):
    res = []
    for a in arts:
        if ma['name'] == a['name'] and ma['txt'] == a['txt']:
            res.append(a)
    return res

def fixWikiMatched(path, wtitles, dfd, N):
    arts = loadDict('dicts/'+path)
    c = 0
    for a in arts:
        print 'at', c, 'from', path, 'proc', os.getpid()
        c+=1
        for m in a['matched']:
            if wiki.isDisambiguationPage(m):
                print 'disambig'
                a['matched'] = wikiMatcher.matchArticle(a, wtitles, dfd, N)
                break
            
            elif wiki.isMissing(m):
                print 'missing'
                a['matched'] = wikiMatcher.matchArticle(a, wtitles, dfd, N)
                break
            
    print 'saving', path
    saveDict(arts, 'dicts/fixed_'+path)
            
def fixAllWikiMatched():
    f = open('wikipedia/wikititles')
    wikititles = [l[:-1].lower() for l in f]
    f.close()
    wikititles.sort()
    
    f = open('mcl/dfd.pkl')
    dfd = pickle.load(f)
    f.close()
    
    paths = ['9_wiki_full.pkl','3_wiki_full.pkl', '11_wiki_full.pkl']
    #paths = ['15_wiki_full.pkl']
    
    map(lambda x: fixWikiMatched(x, wikititles, dfd, 36265), paths)

def getDirectReferences(arts, out):
    mentionReferencer.getOffsetDirectMentions(arts)
    serializeGraph(arts, out, 'direct_refs')

def deserializeReferences(arts, graph, key='refs'):
    ad= dict()
    for a in arts:
        ad[a['id']] = a

    r = open(graph)
    for line in r:
        ids = line[:-1].split(' ')
        art = ad[ids[0]]
        
        art[key] = []
        for id in ids[1:]:
            art[key].append((ad[id]['name'], ad[id]['id']))
    r.close()

def getPrankPage(arts, out):
    sart = sorted(arts, key=lambda x: x['rank'], reverse=True)
    w = open(out, 'w')
    for a in sart:
        w.write(a['name']+':'+str(a['rank'])+'\n')
    w.close()

def split_rev_wiki(path, lim=200):
    revw = loadDict(path)
    c = 0
    res = dict()
    for k in revw.keys():
        res[k] = revw[k]
        c+=1

        if c % lim == 0:
            print c
            res['sizes'] = revw['sizes']
            name = path.split('.')[0]+'_'+str(c/lim)+'.pkl'
            saveDict(res, name)
            res = dict()

def get_titles(path, out):
    arts = loadDict(path)
    w = open(out, 'w')
    for a in arts:
        w.write(a['name'])
        w.write('\n')
    w.close()

if __name__ == '__main__':
    split_rev_wiki('dicts/rev_wiki.pkl')
    #get_titles('dicts/sample_9.pkl', 'sample_9_titles')
    
    
    '''
    arts = loadDict('dicts/fixed_11_wiki_full.pkl')
    getDirectReferences(arts, 'direct_refs_graph_ed_11.txt')
    #fixAllWikiMatched()

    #serializeGraph('../../shared/edition-15/articles.pickle', 'java/current_graph_fixed.txt')
    #deserializePRank('../../shared/edition-15/articles.pickle', 'java/current_rank_fixed.txt', 'current_prank_fixed.txt')
    #deserializeWiki('java/wiki_rank.txt', '../data/wiki/titles-sorted.txt', 'wiki_prank.txt')
    #desMlogs('c_mlog2', '../../shared/edition-11/articles.pickle', '11.pkl')
    #includeRanks('15.pkl', '15_rank.txt')
    #pickleWiki('../data/wiki/titles-sorted.txt', 'java/wiki_rank.txt', 'wiki.pkl')


    arts = loadDict('dicts/articles_fixed.pkl')
    #serializeGraph(arts, 'java/11_graph_fixed.txt')
    includeRanks(arts, 'java/11_rank_fixed.txt')
    normalizeRank(arts)
    saveDict(arts, 'dicts/11.pkl')
    arts.sort(key=lambda x: x['rank'], reverse=True)
    for a in arts[:20]:
        print a['id'], a['ref'][:5], a['name']


    arts = loadDict('dicts/11_wiki_full.pkl')
    include11Ids(arts)
    #includeMatched(arts, m_arts)
    saveDict(arts, 'dicts/11.pkl')
    #arts.sort(key=lambda x: int(x['id']))
    #saveDict(arts, '../../shared/edition-11/articles.pickle')
    #print 'fixed ids for m_arts'

    #switchRefs(o_arts, arts)
    #print "fixed refs"
    #serializeGraph(arts, 'java/11_graph_fixed.txt')

    #includeRanks(arts, 'java/11_rank_fixed.txt')
    #normalizeRank(arts)
    #saveDict(arts, 'dicts/11.pkl')

    arts = loadDict('dicts/11.pkl')
    includeRanks(arts, 'java/11_rank_fixed.txt')
    normalizeRank(arts)
    saveDict(arts, 'dicts/11.pkl')
    
    
    #o_arts = loadDict('../../shared/edition-11/articles_sam.pickle')
    #switchRefs(o_arts, arts)
    #includeRanks(arts, 'java/11_rank_fixed.txt')
   
    #serializeGraph(arts, 'java/11_graph_fixed.txt')
    #saveDict(arts, 'dicts/11_fixed.pkl')
    #include11Ids(arts)
    
    
    #saveDict(arts, 'dicts/11.pkl')
    #fix11Ref(arts, '../../shared/edition-11/articles.pickle')
    
    '''
   
