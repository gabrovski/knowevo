from django.db import models

class MasterArticle(models.Model):
    name = models.CharField(max_length=256)    

class Article(models.Model):
    name = models.CharField(max_length=256)
    art_id = models.IntegerField()
    art_ed = models.IntegerField()
    text = models.TextField()
    
    prank = models.FloatField()
    volume_score = models.FloatField()

    def __unicode__(self):
        return self.name
    
class Match(models.Model):
    article = models.ForeignKey(MasterArticle)
    match_ed = models.IntegerField()
    match_id = models.IntegerField()
    match_score = models.FloatField()

    def __unicode__(self):
        #match = Article.objects.get(art_id=self.match_id, art_ed=self.match_ed)
        return str(self.article)+','+str(self.match_id)

class Reference(models.Model):
    article = models.ForeignKey(Article)
    ref_ed = models.IntegerField()
    ref_id = models.IntegerField()
    
    def __unicode__(self):
        ref = Article.objects.get(art_id=self.ref_id, art_ed=ref_ed)
        return ref.name



