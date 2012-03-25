import re, os, sys
sys.path.append('/home/sasho/cs/knowevo')
sys.path.append('/home/sasho/cs')
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from gravebook.models import Article, Category, Other


tpat    = re.compile('title="(.+?)"')
idpat   = re.compile('id="(.+?)"')
imgpat  = re.compile('image="(.+?)"')
bpat    = re.compile('birth="(.+?)"')
dpat    = re.compile('death="(.+?)"')
plpat   = re.compile('people_links="(.+?)"')
olpat   = re.compile('other_links="(.+?)"')
catpat  = re.compile('categories="(.+?)"')

def insert_xml(path):
    f = open(path)
    count = 0

    for line in f:
        count += 1
        print count
        if count == 10:
            pass

        title  = tpat.search(line).group(1)
        wid    = int(idpat.search(line).group(1))
        img    = imgpat.search(line).group(1)
        birth  = int(bpat.search(line).group(1))
        death  = int(dpat.search(line).group(1))
        cats   = catpat.search(line).group(1).split('|')
        others = olpat.search(line).group(1).split('|')
        
        art = Article(name=title, wid=wid, image=img, birth=birth, death=death)
        art.save()
    f.close()

def build_people_graph(path):
    f = open(path)
    count = 0

    for line in f:
        count += 1
        print count
        if count == 10:
            pass

        title  = tpat.search(line).group(1)
        people = plpat.search(line).group(1)
        
        us = Article.objects.get(name=title)

        for link in people.split('|'):
            if link == '':
                continue
            try:
                them = Article.objects.get(name=link)
            except:
                continue
            
            if us.death < them.birth:
                us.influenced.add(them)
                them.influences.add(us)
            elif us.birth > them.death:
                us.influences.add(them)
                them.influenced.add(us)
            else:
                us.peers.add(them)
                them.peers.add(us)
            them.save()
        us.save()
            
    f.close()
    
if __name__ == '__main__':
    build_people_graph('_data/test-people_articles_filtered.txt')
