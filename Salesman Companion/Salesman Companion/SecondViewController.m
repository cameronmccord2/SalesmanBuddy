//
//  SecondViewController.m
//  Salesman Companion
//
//  Created by Taylor McCord on 9/15/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import "SecondViewController.h"

@interface SecondViewController ()

@end

@implementation SecondViewController

@synthesize imagePickerController;
@synthesize imageView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.title = NSLocalizedString(@"New Item", @"New Item");
        self.tabBarItem.image = [UIImage imageNamed:@"second"];
    }
    return self;
}
							
- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(IBAction)takePicture:(id)sender{
        // Lazily allocate image picker controller
        if (!imagePickerController) {
            imagePickerController = [[UIImagePickerController alloc] init];
            
            // If our device has a camera, we want to take a picture, otherwise, we just pick from
            // photo library
            if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]){
                [imagePickerController setSourceType:UIImagePickerControllerSourceTypeCamera];
            }else{
                [imagePickerController setSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
            }
            
            // image picker needs a delegate so we can respond to its messages
            [imagePickerController setDelegate:self];
        }
        // Place image picker on the screen
        [self presentViewController:imagePickerController animated:YES completion:nil];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    [self dismissViewControllerAnimated:YES completion:nil]; //Do this first!!
    [imageView setImage:[info objectForKey:UIImagePickerControllerOriginalImage]];
    
    //image = [ImageHelpers imageWithImage:image scaledToSize:CGSizeMake(480, 640)];

//    [imageView setImage:image];
}

@end
