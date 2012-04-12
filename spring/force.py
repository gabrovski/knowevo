'''
By Sasho

Implements a force based algorithm outlined here:
http://www.cs.brown.edu/~rt/gdhandbook/chapters/force-directed.pdf
'''

class SpringBox:
    def __init__(self, objects, width, height, charge, time_step, kfn):
        self.objects   = objects 
        self.width     = width
        self.height    = height
        self.charge    = charge
        self.time_step = time_step
        self.kfn       = kfn
        
        self.init_positions()

        def init_positions(self):
            '''
            sets the initial positions for the objects for the given width and height
            of the canvas
            sets the force, acceleration, velocity, distance, charge and mass fields
            '''
            pass
        
        def clear_forces(self):
            '''
            clears force vector for each object. needed to reset at each iteration
            '''
            pass

        def compute_repulsive_force(self):
            '''
            computes a repulsive force for all pairs with provided charge
            uses Colonic force to update the force vector
            '''
            pass

        def compute_attractive_force(self):
            '''
            computes the spring force for all neighbors of each object
            kfn returns the spring constant for a pair of objects.
            if the pair is not neighbors then kfn returns None
            '''
            pass

        def move(self):
            '''
            moves according ot acceleration for given time stamp
            keeps track of momentum
            '''
            pass

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
