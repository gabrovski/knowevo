from django.db import models

class Category(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)
    size = models.IntegerField(blank=True, null=True)

    def __unicode__(self):
        return self.name

class Other(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)


class Article(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)
    wid   = models.BigIntegerField()
    birth = models.IntegerField(blank=True, null=True)
    death = models.IntegerField(blank=True, null=True)
    image = models.CharField(max_length=512, default='null')
    
    people = models.ManyToManyField('self')
    peers  = models.ManyToManyField('self')    

    categories  = models.ManyToManyField(Category)
    other_links = models.ManyToManyField(Other)

    linked_by = models.ManyToManyField('self')
    
    vscore = models.FloatField(default=0.0)

    #100 means wikipedia. hacky slashy
    art_ed = models.IntegerField(default=1000)
    text = models.TextField(default='')
    match_master = models.ForeignKey('self', blank=True, null=True)
        
    def __unicode__(self):
        return self.name


