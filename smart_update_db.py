import re, os, sys, traceback
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from gravebook.models import Article, Category, Other
from django.db import transaction


tpat    = re.compile('title="(.+?)"')
idpat   = re.compile('id="(.+?)"')
imgpat  = re.compile('image="(.+?)"')
bpat    = re.compile('birth="(.+?)"')
dpat    = re.compile('death="(.+?)"')
plpat   = re.compile('people_links="(.+?)"')
olpat   = re.compile('other_links="(.+?)"')
catpat  = re.compile('categories="(.+?)"')

START = -1
LIMIT = -1

def insert_xml(path):
    f = open(path)
    count = 0

    transaction.enter_transaction_management()

    for line in f:
        count += 1
        if count < START:
            continue

        print count,
        
        if LIMIT > 0 and count == LIMIT:
            break

        try:
            title  = tpat.search(line).group(1)
            wid    = int(idpat.search(line).group(1))
            img    = imgpat.search(line).group(1)
            birth  = int(bpat.search(line).group(1))
            death  = int(dpat.search(line).group(1))
            #cats   = catpat.search(line).group(1).split('|')
            #others = olpat.search(line).group(1).split('|')
        
            art = Article(name=title, wid=wid, image=img, birth=birth, death=death)
            art.save()
            print title
        except:
            print "bad"
            #traceback.print_exc(file=sys.stdout)
            
            transaction.rollback()
        else:
            transaction.commit()
            

    transaction.leave_transaction_management()
    f.close()

def build_people_graph(path):
    f = open(path)
    count = 0

    transaction.enter_transaction_management()
    for line in f:
        count += 1
        if count < START:
            continue

        print count,
        if LIMIT > 0 and count == LIMIT:
            break

        try:
            title  = tpat.search(line).group(1)
            people = plpat.search(line).group(1)
        
            us = Article.objects.get(name=title)

            for link in people.split('|'):
                if link == '':
                    continue
                try:
                    them = Article.objects.get(name=link)
                except:
                    transaction.rollback()
                    continue
            
                if us.death < them.birth and us.death != -1:
                    us.influenced.add(them)
                    them.influences.add(us)
                elif us.birth > them.death and them.death != -1:
                    us.influences.add(them)
                    them.influenced.add(us)
                else:
                    us.peers.add(them)
                    them.peers.add(us)
                them.save()
                us.save()
                transaction.commit()
            print us.name
        except:
            print 'bad'
            transaction.rollback()
        else:
            transaction.commit()
            
    transaction.leave_transaction_management()
    f.close()
    
if __name__ == '__main__':
    #insert_xml('_data/people_articles_filtered.txt')
    build_people_graph('_data/people_articles_filtered.txt')
