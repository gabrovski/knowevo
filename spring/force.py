'''
By Sasho

Implements a force based algorithm outlined here:
http://www.cs.brown.edu/~rt/gdhandbook/chapters/force-directed.pdf
'''
import math

class SpringBox:
    def __init__(self, objects, width, height, charge, mass, time_step, kfn):
        self.objects   = objects 
        self.width     = width
        self.height    = height
        self.charge    = charge
        self.mas       = mass
        self.time_step = time_step
        self.kfn       = kfn
        
        self.init_positions()


    def init_positions(self):
        '''
        sets the initial positions for the objects for the given width and height
        of the canvas
        sets the force, acceleration, velocity, distance, charge and mass fields
        '''
        
        A = self.width * self.height      #total area
        N = len(self.objects)             #number of squares
        S = A / N                         #square area
        R = S ** 0.5                      #object radius
        
        curr_x = 0
        curr_y = R
        for o in self.objects:
            o.pos    = [0.0, 0.0]
            o.force  = [0.0, 0.0]
            o.acc    = [0.0, 0.0]
            o.vel    = [0.0, 0.0]
            o.mass   = self.mass+0.0
            o.charge = self.charge+0.0

            o.pos[0] = curr_x + R
            o.pos[1] = curr_y
            
            curr_x += R                   #center square
            
            if curr_x + R > self.width:   #wrap to next line
                curr_x = 0
                curr_y += 2 * R
                
        
    def clear_forces(self):
        '''
        clears force vector for each object. needed to reset at each iteration
        '''
        for o in self.objects:
            o.force = [0.0, 0.0]
                

    def compute_repulsive_force(self):
        '''
        computes a repulsive force for all pairs with provided charge
        uses Coloumbic force to update the force vector
        '''
        for i in xrange(len(self.objects)):
            for j in xrange(i+1, len(self.objects)):
                
                v = self.objects[i]
                u = self.objects[j]
                
                dx          = v.pos[0] - u.pos[0]
                sign        = dx / math.fabs(dx)
                v.force[0] += sign * v.charge * u.charge / dx**2
                u.force[0] += (-1) * sign * v.charge * u.charge / dx**2
                
                dy          = v.pos[1] - u.pos[1]
                sign        = dy / math.fabs(dy)
                v.force[1] += sign * v.charge * u.charge / dy**2
                u.force[1] += (-1) * sign * v.charge * u.charge / dy**2
                

    def compute_attractive_force(self):
        '''
        computes the spring force for all neighbors of each object
        using Hooke's Law: F = -k*d
        kfn returns the spring constant for a pair of objects.
        if the pair is not neighbors then kfn returns None
        '''
        for i in xrange(len(self.objects)):
            for j in xrange(i+1, len(self.objects)):
                
                v = self.objects[i]
                u = self.objects[j]
                
                k = self.kfn(v, u)
                if k == None:
                    continue
                
                dx          = v.pos[0] - u.pos[0]
                v.force[0] += (-1) * dx * k
                u.force[0] += dx * k
                
                dy          = v.pos[1] - u.pos[1]
                v.force[1] += (-1) * dy * k
                u.force[1] += dy * k
                
    def move(self):
        '''
        moves according ot acceleration for given time stamp
        keeps track of momentum
        
        S = Vo*t + 1/2*a*t^2
        V = Vo + a*t
        '''
        for o in self.objects:
            o.acc = map(lambda x: x / o.mass, o.force)
            o.pos = map(lambda x, y: 
                        x * self.time_step + y / 2 * self.time_step ** 2,
                        o.vel, o.acc)
            
            o.vel = map(lambda x, y: x + y * self.time_step, o.acc)
            
    def move_to_equillibrium(self, R):
        '''
        sets forces and moves for a time_stamp R times. then assumes equillibirum
        has been reached
        '''
        for x in xrange(R):
            self.clear_forces()
            self.compute_repulsive_force()
            self.compute_attractive_force()
            self.move()
