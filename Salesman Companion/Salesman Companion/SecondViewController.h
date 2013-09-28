//
//  SecondViewController.h
//  Salesman Companion
//
//  Created by Taylor McCord on 9/15/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SecondViewController : UIViewController <UIImagePickerControllerDelegate, UINavigationControllerDelegate>

@property(nonatomic, strong)IBOutlet UIImageView *imageView;
@property(nonatomic, strong)IBOutlet UITextField *insuranceProvider;
@property(nonatomic, strong)IBOutlet UITextField *insuranceAgent;

@property(nonatomic, strong)UIImagePickerController *imagePickerController;
-(IBAction)takePicture:(id)sender;
-(IBAction)save:(id)sender;

@end
