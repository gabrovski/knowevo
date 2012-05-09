try:
    import os
    os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'
    from incunabula.models import Article, MasterArticle
except:
    print 'django settings not loaded'
    pass


import serialize as ser
import wiki
import art_measure as a_m
from params import PARAMS

import bisect
from abc import abstractmethod


###check if articles is to be flagged
def is_valid_article(ed, birth):
    prev = 30
    return (
        (ed == 3 and birth < 1797-prev) or
        (ed == 9 and birth < 1875-prev) or
        (ed == 11 and birth < 1910-prev) or
        (ed == 15))


def is_missing(ed, name):
    try:
        art = Article.objects.get(match_master__name=name, art_ed=ed)
        return False
    except:
        #raise
        return True


###retrieve bad articles
def get_missing_articles(top):
    missing = {3:[],9:[],11:[],15:[]}
    
    f = open(top)
    for line in f:
        splits = line.strip().split(' ')
        name, birth = splits[0], int(splits[1])
            
        if is_valid_article(3, birth) and is_missing(3, name):
            missing[3].append(name)
            
        if is_valid_article(9, birth) and is_missing(9, name):
            missing[9].append(name)

        if is_valid_article(11, birth) and is_missing(11, name):
            missing[11].append(name)

        if is_valid_article(15, birth) and is_missing(15, name):
            missing[15].append(name)
                
    f.close()

    for ed in [3, 9, 11, 15]:
        for m in missing[ed]:
            print m, ed
        

def get_edition_matches(ed):
    for art in Article.objects.filter(art_ed=ed).iterator():
        print art.name    

def loadOccupations():
    name = '_data/occp'
    res = set()
    f = open(name)
    for line in f:
        res.add(line[:-1].lower())
    f.close()
    return res

#wtfidf function
def get_wt():
    dfd = ser.loadDict('_data/15_dfd.pkl')
    occp = loadOccupations()
    fn = lambda x, y: a_m.getTFScore(x,y,dfd,81697, a_m.getWTFIDFd, PARAMS, occp=occp)
    return fn


###candidate finding classes follow
class Dealer:
    def __init__(self, exisiting_path, edition):
        self.existing = self.load_existing_names(exisiting_path) #wikipedia articles already matched to
        self.edition = edition

    @abstractmethod
    def load_existing_names(self, fname):
        pass
    
    def format_name(self, name):
        fname = name
        if self.edition == 3:
            parts = name.split('_')
            ln = len(parts)
            fname = ' '.join([parts[ln-1]]+parts[:ln-1]).lower()

        return fname

    def get_candid_indeces(self, wname):
        wname = self.format_name(wname)
        index = bisect.bisect_left(self.existing, wname)
        return index-1, index

    def get_candids(self, wname):
        i1, i2 = self.get_candid_indeces(wname)
        return [self.existing[i1], self.existing[i2]]

###wiki only candids
class WikiDealer(Dealer):
    def __init__(self, exist_wiki_path):
        Dealer.__init__(self, exist_wiki_path, 'wiki')

    #Override
    def load_existing_names(self, fname):
        res = []

        f = open(fname)
        for line in f:
            res.append(line.strip().split('#')[2])

        res.sort()
        return res


###candid generation
class CandidDealer(Dealer):
    def __init__(self, exist_brit_path, edition, wikidealer, pickle_path):
        Dealer.__init__(self,exist_brit_path, edition)
        self.wikidealer = wikidealer
        self.arts = ser.loadDict(pickle_path)

    #Override
    def load_existing_names(self, fname):
        res = []
        
        f = open(fname)

        for line in f:
            res.append((line.strip().lower(), line[:-1]))
        f.close()

        res.sort(key=lambda x: x[0])

        self.existing_tuples = res
        return map(lambda x: x[0], res)

    def get_brit_candids_from_wiki_candids(self, wname):
        res  = []
        wis = self.wikidealer.get_candid_indeces(wname)
        #print self.wikidealer.existing[wis[0]-1:wis[0]+1]

        c = 0
        for wi in wis:
            while c < 2:
                try:
                    art = Article.objects.get(match_master__name=self.wikidealer.existing[wi], art_ed=self.edition)
                    #print self.wikidealer.existing[wi]
                    res.append(art.name)
                    break
                except:
                    if c == 0: wi-=1
                    else: wi+=1
            c+=1
        return map(str, res)

    #Override
    def get_candids(self, wname):
        i1, i2 = self.get_candid_indeces(wname)
        if i2 > len(self.existing_tuples)-1:
            i2 = len(self.existing_tuples)-1
            i1 = i2 -1
        return [self.existing_tuples[i1][1], self.existing_tuples[i2][1]]

    def get_all_candids(self, wname):
        bcandids = self.get_brit_candids_from_wiki_candids(wname) + self.get_candids(wname)
        return map(str, bcandids)

    def get_best_candid(self, wname):
        wtext = wiki.getArticleText(wname)
        #candids = self.get_brit_candids_from_wiki_candids(wname)
        candids = self.get_all_candids(wname)
        indeces = []
        
        c = 0
        for art in self.arts:
            if str(art['name']) in candids:
                indeces.insert(candids.index(art['name']),c)
            c+=1

        c = 0
        title_texts = []
        while c < 2:
            for i in xrange(indeces[c], indeces[c+1]+1):
                title_texts.append((self.arts[i]['name'], self.arts[i]['txt']))
            c+=2
            
        occp = loadOccupations()

        scandids = a_m.getCombinedScores(wtext, title_texts, FN, occp)
        if scandids == []:
            return None

        return scandids[0]

class SimpleCandidDealer(Dealer):
    def __init__(self, pickle_path, edition):
        Dealer.__init__(self, pickle_path, edition)
        self.fn = get_wt()
        self.occp = loadOccupations()

    #override
    def load_existing_names(self, name):
        return ser.loadDict(name)

    #override
    def get_candids(self, wname):
        last = wname.split('_of_')
        if len(last) == 1:
            last = wname.split(',_')

        if len(last) > 1:
            last = last[0].lower()
        else:
            last = wname.split('_')[::-1][0].lower()
        res = []

        while len(res) == 0:
            res += filter(lambda x: last in x['name'].lower(), self.existing)
            last = last[:len(last)-1]
            #print last, 
        return res

    def get_best_candid(self, wname):
        candids = self.get_candids(wname)
        wtext = wiki.getArticleText(wname)
        
        title_texts = []
        for art in candids:
                title_texts.append((art['name'], art['txt']))

        scandids = a_m.getCombinedScores(wtext, title_texts, self.fn, self.occp, vt=True)
        if scandids == []:
            return None

        return scandids[0]
            

def get_all_corrections(path,ed,cdealer):
    f = open(path)
    f.readline()
    for line in f:
        name, edition = line.strip().split(' ')
        if int(edition) == ed:
            print name, cdealer.get_best_candid(name)
            #print name, cdealer.get_candids(name)
    f.close()

if __name__=='__main__':
    #get_missing_articles('top200people')
    #get_edition_matches(3)

    #wd = WikiDealer('_data/matchedarts.txt')
    #cd = CandidDealer('_data/ed3arts', 3, wd, '_data/people_ed_3.pkl')
    #cd.get_best_candid('William_Shakespeare')
    #get_all_corrections('_data/tobefilledpeople', 3, cd)

    cd3 = SimpleCandidDealer('_data/3_wiki_full.pkl', 3)
    cd9 = SimpleCandidDealer('_data/9_wiki_full.pkl', 9)
    cd11 = SimpleCandidDealer('_data/11_wiki_full.pkl', 11)
    #print cd.get_candids('William_Shakespeare')
    get_all_corrections('_data/tobefilledpeople', 3, cd)
